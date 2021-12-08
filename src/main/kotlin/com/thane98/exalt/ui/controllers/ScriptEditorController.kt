package com.thane98.exalt.ui.controllers

import com.thane98.exalt.model.CompileResult
import com.thane98.exalt.ui.misc.AsyncSyntaxHighlighter
import com.thane98.exalt.ui.misc.EditorKeyEventProcessor
import com.thane98.exalt.ui.misc.FileUtils
import com.thane98.exalt.ui.model.ScriptModel
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Tab
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory
import java.net.URL
import java.util.*

class ScriptEditorController(
    private val model: ScriptModel,
) : Initializable {
    @FXML
    private lateinit var tab: Tab
    @FXML
    private lateinit var codeArea: CodeArea
    @FXML
    private lateinit var findTextField: TextField
    @FXML
    private lateinit var replaceTextField: TextField
    @FXML
    private lateinit var findReplaceBar: HBox

    private val saveFileChooser = FileChooser()
    private val compiledFileChooser = FileChooser()
    private lateinit var syntaxHighlighter: AsyncSyntaxHighlighter

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        saveFileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Exalt Script", "*.exl"))
        compiledFileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Script Binary", "*.cmb"))

        findReplaceBar.managedProperty().bind(findReplaceBar.visibleProperty())

        codeArea.paragraphGraphicFactory = LineNumberFactory.get(codeArea)
        syntaxHighlighter = AsyncSyntaxHighlighter(codeArea)
        codeArea.replaceText(model.contentsProperty.value)
        codeArea.undoManager.forgetHistory()
        EditorKeyEventProcessor.setup(codeArea)

        model.compiledFileNameProperty.addListener { _ -> onScriptFileNameChanged() }
        model.sourceFileNameProperty.addListener { _ -> onScriptFileNameChanged() }
        model.contentsProperty.bind(codeArea.textProperty())

        tab.setOnCloseRequest { terminateAnalyzers() }

        onScriptFileNameChanged()
    }

    @FXML
    private fun onFindNext() {
        if (findTextField.text.isNotEmpty()) {
            findAndSelectNextOccurrence(findTextField.text)
        }
    }

    @FXML
    private fun onReplaceNext() {
        if (findTextField.text.isNotEmpty()) {
            findAndReplaceNextOccurrence(findTextField.text, replaceTextField.text)
        }
    }

    @FXML
    private fun onReplaceAll() {
        if (findTextField.text.isNotEmpty()) {
            findAndReplaceAllOccurrences(findTextField.text, replaceTextField.text)
        }
    }

    @FXML
    private fun onCloseFindReplaceBar() {
        findReplaceBar.isVisible = false
    }

    fun showFindReplaceBar() {
        findReplaceBar.isVisible = true
        findTextField.requestFocus()
    }

    fun undo() {
        codeArea.undo()
    }

    fun redo() {
        codeArea.redo()
    }

    fun cut() {
        codeArea.cut()
    }

    fun paste() {
        codeArea.paste()
    }

    fun copy() {
        codeArea.copy()
    }

    fun delete() {
        codeArea.deleteText(codeArea.selection)
    }

    fun selectAll() {
        codeArea.selectAll()
    }

    fun unselect() {
        codeArea.selectRange(codeArea.caretPosition, codeArea.caretPosition)
    }

    fun terminateAnalyzers() {
        syntaxHighlighter.terminate()
    }

    fun saveScriptUnderNewName() {
        val tmp = model.sourceFilePath
        model.sourceFilePath = ""
        try {
            saveScript()
        } catch (ex: Exception) {
            model.sourceFilePath = tmp
            throw ex
        }
    }

    fun saveScript() {
        if (!model.canSave()) {
            val choice = saveFileChooser.showSaveDialog(codeArea.scene.window)
            if (choice != null) {
                model.sourceFilePath = choice.absolutePath
            } else {
                return
            }
        }
        FileUtils.saveScript(model.contents, model.sourceFilePath)
    }

    fun compileAndSaveScriptUnderNewName() {
        val tmp = model.compiledFilePath
        model.compiledFilePath = ""
        try {
            if (compileAndSaveScript() == null) {
                model.compiledFilePath = tmp
            }
        } catch (ex: Exception) {
            model.compiledFilePath = tmp
            throw ex
        }
    }

    fun compileAndSaveScript(): CompileResult? {
        if (!model.canCompile()) {
            val choice = compiledFileChooser.showSaveDialog(codeArea.scene.window)
            if (choice != null) {
                model.compiledFilePath = choice.absolutePath
            } else {
                return null
            }
        }
        return FileUtils.compileAndSaveScript(
            model.contents,
            model.compiledFileName,
            model.compiledFilePath
        )
    }

    private fun findAndSelectNextOccurrence(target: String) {
        val selectIndex = findText(target)
        if (selectIndex != -1) {
            codeArea.selectRange(selectIndex, selectIndex + target.length)
            codeArea.requestFollowCaret()
        }
    }

    private fun findAndReplaceNextOccurrence(target: String, replacement: String) {
        val targetIndex = findText(target)
        if (targetIndex != -1) {
            codeArea.replaceText(targetIndex, targetIndex + target.length, replacement)
            codeArea.requestFollowCaret()
        }
    }

    private fun findAndReplaceAllOccurrences(target: String, replacement: String) {
        codeArea.replaceText(codeArea.text.replace(target, replacement))
    }

    private fun onScriptFileNameChanged() {
        if (model.compiledFileName.isNotEmpty()) {
            tab.text = model.compiledFileName
        } else if (model.sourceFileName.isNotEmpty()) {
            tab.text = model.sourceFileName
        } else {
            tab.text = "{no name}"
        }
    }

    private fun findText(target: String): Int {
        val start = codeArea.selection.end
        val selectIndex = codeArea.text.indexOf(target, start)
        return if (selectIndex == -1 && start != 0) codeArea.text.indexOf(target) else selectIndex
    }
}