package editor

import decompiler.decompile
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.scene.control.Tab
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory
import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder
import org.reactfx.Subscription
import java.io.File
import java.nio.charset.StandardCharsets
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
    var sourceFile: File? = null
    var destFile: File? = null
    val codeArea = CodeArea()

    constructor(sourceFile: File, experimental: Boolean = false): this(sourceFile.name) {
        this.sourceFile = sourceFile
        if (sourceFile.path.endsWith(".cmb"))
            destFile = sourceFile
        codeArea.replaceText(0, 0, tryOpenFile(sourceFile, experimental))
    }

    init {
        content = VirtualizedScrollPane(codeArea)
        codeArea.paragraphGraphicFactory = LineNumberFactory.get(codeArea)
        subscription = codeArea.multiPlainChanges()
            .successionEnds(Duration.ofMillis(200))
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

    fun stopProcessing() {
        subscription.unsubscribe()
        executor.shutdown()
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
}
