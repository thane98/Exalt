package com.thane98.exalt.editor

import com.thane98.exalt.decompiler.decompile
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.collections.ListChangeListener
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Popup
import org.controlsfx.control.PopOver
import org.controlsfx.glyphfont.Glyph
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory
import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder
import org.reactfx.Subscription
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors
import java.util.regex.Pattern

class ScriptEditor(title: String) : Tab(title) {
    companion object {
        private val KEYWORDS = arrayOf(
            "event", "func", "const", "var",
            "let", "for", "while", "if",
            "else", "match", "return", "yield",
            "array", "goto", "label", "fix", "float"
        )

        private val KEYWORD_PATTERN = "\\b(" + KEYWORDS.joinToString("|") + ")\\b"
        private const val STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\""
        private const val COMMENT_PATTERN = "#[^\n]*"
        private const val FRAME_REF_PATTERN = "\\$[0-9]+"

        private val PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<FRAMEREF>" + FRAME_REF_PATTERN + ")"
        )

        private val WHITESPACE = Pattern.compile("^\\s+")
    }

    private val executor = Executors.newSingleThreadExecutor()
    private var subscription: Subscription
    private val completionPopOver = PopOver()
    private val completionContents = ListView<String>()
    var sourceFile: File? = null
    var destFile: File? = null
    val codeArea = CodeArea()
    lateinit var findReplaceBar: HBox

    constructor(sourceFile: File, experimental: Boolean = false) : this(sourceFile.name) {
        this.sourceFile = sourceFile
        if (sourceFile.path.endsWith(".cmb"))
            destFile = sourceFile
        codeArea.replaceText(0, 0, tryOpenFile(sourceFile, experimental))
        codeArea.undoManager.forgetHistory() // Don't allow user to hit undo in the initial window
    }

    init {
        content = createEditorContent()
        codeArea.paragraphGraphicFactory = LineNumberFactory.get(codeArea)
        subscription = codeArea.multiPlainChanges()
            .successionEnds(Duration.ofMillis(400))
            .supplyTask(this::computeHighlightingAsync)
            .awaitLatest(codeArea.multiPlainChanges())
            .filterMap { t ->
                if (t.isSuccess)
                    Optional.of(t.get())
                else
                    Optional.empty()
            }
            .subscribe(this::applyHighlighting)

        // Replace tabs with spaces
        codeArea.addEventFilter(KeyEvent.KEY_PRESSED) { e ->
            val targetParagraph = codeArea.getParagraph(codeArea.currentParagraph)
            when {
                e.code == KeyCode.TAB -> {
                    codeArea.insertText(codeArea.caretPosition, "    ")
                    e.consume()
                }
                e.code == KeyCode.ENTER -> {
                    val matcher = WHITESPACE.matcher(targetParagraph.segments[0])
                    if (matcher.find()) {
                        Platform.runLater { codeArea.insertText(codeArea.caretPosition, matcher.group()) }
                    }
                }
                e.code == KeyCode.BACK_SPACE -> {
                    if (atEndOfIndent()) {
                        codeArea.deleteText(codeArea.caretPosition - 4, codeArea.caretPosition)
                        e.consume()
                    }
                }
            }
        }
        this.setOnClosed { stopProcessing() }
        configureCompletionPopOver()
    }

    private fun configureCompletionPopOver() {
        completionPopOver.contentNode = completionContents
        completionPopOver.arrowLocation = PopOver.ArrowLocation.TOP_CENTER
        completionContents.items.addAll("Test", "Test 2")
        completionContents.prefHeightProperty()
            .bind(Bindings.size(completionContents.items).multiply(36).add(2))
        completionPopOver.scene.addEventFilter(KeyEvent.KEY_PRESSED) { keyEvent ->
            if (keyEvent.code == KeyCode.ESCAPE) {
                completionPopOver.hide()
                keyEvent.consume()
            } else if (keyEvent.code == KeyCode.ENTER) {
                val target = getCurrentWord()
                val replacement = completionContents.selectionModel.selectedItem
                codeArea.replaceText(codeArea.caretPosition - target.length, codeArea.caretPosition, replacement)
                completionPopOver.hide()
                keyEvent.consume()
            }
        }
    }

    private fun createEditorContent(): Node {
        val root = VBox()
        findReplaceBar = createFindReplaceBar()
        val findSeparator = Separator()
        val editor = VirtualizedScrollPane(codeArea)
        root.setMinSize(0.0, 0.0)
        root.children.add(findReplaceBar)
        root.children.add(findSeparator)
        root.children.add(editor)
        VBox.setVgrow(editor, Priority.ALWAYS)
        findReplaceBar.managedProperty().bind(findReplaceBar.visibleProperty())
        findReplaceBar.isVisible = false
        findSeparator.managedProperty().bind(findReplaceBar.visibleProperty())
        findSeparator.visibleProperty().bind(findReplaceBar.visibleProperty())
        return root
    }

    private fun createFindReplaceBar(): HBox {
        val bar = HBox()
        bar.alignment = Pos.CENTER_LEFT
        bar.spacing = 5.0
        bar.padding = Insets(5.0, 5.0, 5.0, 5.0)

        val findField = TextField()
        findField.promptText = "Find..."
        findField.setOnAction { findNext(findField.text) }
        val replaceField = TextField()
        replaceField.promptText = "Replace With..."
        replaceField.setOnAction { replaceNext(findField.text, replaceField.text) }
        val findButton = Button("Find")
        findButton.setOnAction { findNext(findField.text) }
        val replaceButton = Button("Replace")
        replaceButton.setOnAction { replaceNext(findField.text, replaceField.text) }
        val replaceAllButton = Button("Replace All")
        replaceAllButton.setOnAction {
            if (findField.text != null && replaceField.text != null) {
                codeArea.replaceText(codeArea.text.replace(findField.text, replaceField.text))
            }
        }

        val spacer = Pane()
        val closeButton = Button("", Glyph("FontAwesome", "Close"))
        closeButton.setOnAction { bar.isVisible = false }
        bar.children.addAll(findField, replaceField, findButton, replaceButton, replaceAllButton, spacer, closeButton)
        HBox.setHgrow(spacer, Priority.ALWAYS)
        return bar
    }

    private fun findNext(target: String) {
        val selectIndex = findText(target)
        if (selectIndex != -1) {
            codeArea.selectRange(selectIndex, selectIndex + target.length)
            codeArea.scrollYToPixel(codeArea.currentParagraph * 20.0)
        }
    }

    private fun replaceNext(target: String, replacement: String) {
        val targetIndex = findText(target)
        if (targetIndex != -1) {
            codeArea.replaceText(targetIndex, targetIndex + target.length, replacement)
            codeArea.scrollYToPixel(codeArea.currentParagraph * 20.0)
        }
    }

    private fun findText(target: String): Int {
        val start = if (codeArea.selectedText == target) codeArea.selection.end else 0
        val selectIndex = codeArea.text.indexOf(target, start)
        return if (selectIndex == -1 && start != 0) codeArea.text.indexOf(target) else selectIndex
    }

    private fun atEndOfIndent(): Boolean {
        val pos = codeArea.caretPosition
        if (pos - 4 < 0)
            return false
        return codeArea.text.slice(pos - 4 until pos) == "    "
    }

    private fun tryOpenFile(file: File, experimental: Boolean): String {
        return if (file.path.endsWith(".cmb"))
            decompile(file.path, experimental)
        else
            String(Files.readAllBytes(Paths.get(file.path)))
    }

    private fun computeHighlightingAsync(): Task<StyleSpans<Collection<String>>> {
        val text = codeArea.text
        val task = object : Task<StyleSpans<Collection<String>>>() {
            override fun call(): StyleSpans<Collection<String>> {
                return computeHighlighting(text)
            }
        }
        executor.execute(task)
        return task
    }

    private fun applyHighlighting(highlighting: StyleSpans<Collection<String>>) {
        codeArea.setStyleSpans(0, highlighting)
        var revealedPopOver = false
        if (codeArea.caretBounds.isPresent) {
            val currentWord = getCurrentWord()
            if (currentWord.isNotEmpty()) {
                populateCompletionPopOver(currentWord)
                if (completionContents.items.isNotEmpty()) {
                    val bounds = codeArea.caretBounds.get()
                    revealedPopOver = true
                    completionPopOver.show(codeArea, bounds.minX, bounds.maxY)
                }
            }
        }
        if (!revealedPopOver)
            completionPopOver.hide()
    }

    private fun populateCompletionPopOver(prefix: String) {
        completionContents.items.clear()
        for (str in CompletionManager.fatesFunctions) {
            if (str.startsWith(prefix))
                completionContents.items.add(str)
            if (completionContents.items.size >= 10) break
        }
    }

    private fun getCurrentWord(): String {
        val paragraph = codeArea.paragraphs[codeArea.currentParagraph]
        val startIndex = codeArea.caretColumn
        var i = startIndex - 1
        while (i >= 0 && !paragraph.text[i].isWhitespace()) i--
        i += 1
        return paragraph.text.substring(i, startIndex)
    }

    private fun computeHighlighting(text: String): StyleSpans<Collection<String>> {
        val matcher = PATTERN.matcher(text)
        var lastKwEnd = 0
        val spansBuilder = StyleSpansBuilder<Collection<String>>()
        while (matcher.find()) {
            val styleClass = (when {
                matcher.group("KEYWORD") != null -> "keyword"
                matcher.group("STRING") != null -> "string"
                matcher.group("COMMENT") != null -> "comment"
                matcher.group("FRAMEREF") != null -> "frame-ref"
                else -> null
            })!!
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd)
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start())
            lastKwEnd = matcher.end()
        }
        spansBuilder.add(Collections.emptyList(), text.length - lastKwEnd)
        return spansBuilder.create()
    }

    fun stopProcessing() {
        subscription.unsubscribe()
        executor.shutdown()
    }
}
