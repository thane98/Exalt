package com.thane98.exalt.editor

import com.thane98.exalt.compiler.compileFromInMemoryScript
import javafx.application.Platform
import javafx.beans.Observable
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.DragEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.Style
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*

class MainWindowController : Initializable {
    @FXML
    private lateinit var root: VBox
    @FXML
    private lateinit var scriptsPane: TabPane
    @FXML
    private lateinit var toolBarSpacer: Pane
    @FXML
    private lateinit var statusBarSpacer: Pane
    @FXML
    private lateinit var experimentalModeItem: CheckMenuItem
    @FXML
    private lateinit var awakeningModeItem: CheckMenuItem
    @FXML
    private lateinit var closeMenuItem: MenuItem
    @FXML
    private lateinit var saveMenuItem: MenuItem
    @FXML
    private lateinit var saveAsMenuItem: MenuItem
    @FXML
    private lateinit var editMenu: Menu
    @FXML
    private lateinit var saveToolBarItem: Button
    @FXML
    private lateinit var compileToolBarItem: Button
    @FXML
    private lateinit var showToolBarItem: CheckMenuItem
    @FXML
    private lateinit var showStatusBarItem: CheckMenuItem
    @FXML
    private lateinit var showConsoleItem: CheckMenuItem
    @FXML
    private lateinit var console: TextArea
    @FXML
    private lateinit var compileLabel: Label
    @FXML
    private lateinit var progressBar: ProgressBar
    @FXML
    private lateinit var toolBar: ToolBar
    @FXML
    private lateinit var statusBar: ToolBar
    @FXML
    private lateinit var themeGroup: ToggleGroup
    @FXML
    private lateinit var consoleContainer: VBox
    @FXML
    private lateinit var mainSplitPane: SplitPane

    private var nextUntitled = 0
    private val config = Config()

    private val openDialog = FileChooser()
    private val saveDialog = FileChooser()
    private val compileDialog = FileChooser()


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // Configure resizing for console and spacers.
        HBox.setHgrow(toolBarSpacer, Priority.ALWAYS)
        HBox.setHgrow(statusBarSpacer, Priority.ALWAYS)
        scriptsPane.tabs.addListener { _: Observable -> toggleActions() }

        createConfigPropertyBindings()
        toggleActions()
        setupFileDialogs()
    }

    private fun createConfigPropertyBindings() {
        awakeningModeItem.selectedProperty().bindBidirectional(config.awakeningMode)
        experimentalModeItem.selectedProperty().bindBidirectional(config.experimentalMode)
        showToolBarItem.selectedProperty().bindBidirectional(config.showToolBar)
        showStatusBarItem.selectedProperty().bindBidirectional(config.showStatusBar)
        showConsoleItem.selectedProperty().bindBidirectional(config.showConsole)

        toolBar.visibleProperty().bindBidirectional(config.showToolBar)
        toolBar.managedProperty().bind(config.showToolBar)
        statusBar.visibleProperty().bindBidirectional(config.showStatusBar)
        statusBar.managedProperty().bind(config.showStatusBar)
        consoleContainer.visibleProperty().addListener { _: Observable ->
            if (consoleContainer.isVisible)
                mainSplitPane.items.add(consoleContainer)
            else
                mainSplitPane.items.remove(consoleContainer)
        }
        consoleContainer.visibleProperty().bindBidirectional(config.showConsole)
    }

    private fun setupFileDialogs() {
        openDialog.title = "Open File"
        openDialog.extensionFilters.addAll(
            FileChooser.ExtensionFilter("All Supported Types", "*.cmb", "*.exl"),
            FileChooser.ExtensionFilter("Compiled Script", "*.cmb"),
            FileChooser.ExtensionFilter("Exalt Script", "*exl")
        )
        saveDialog.title = "Save Script"
        saveDialog.extensionFilters.add(FileChooser.ExtensionFilter("Exalt Script", "*.exl"))
        compileDialog.title = "Choose compiled script destination."
        compileDialog.extensionFilters.add(FileChooser.ExtensionFilter("Compiled Script", "*.cmb"))
    }

    fun saveAndCloseTabs() {
        for (tab in scriptsPane.tabs) {
            val editor = tab as ScriptEditor
            editor.stopProcessing()
        }
        config.save()
    }

    private fun toggleActions() {
        val noEditorsOpen = scriptsPane.tabs.isEmpty()
        closeMenuItem.isDisable = noEditorsOpen
        saveAsMenuItem.isDisable = noEditorsOpen
        saveMenuItem.isDisable = noEditorsOpen
        saveToolBarItem.isDisable = noEditorsOpen
        compileToolBarItem.isDisable = noEditorsOpen
        for (item in editMenu.items)
            item.isDisable = noEditorsOpen
    }

    @FXML
    private fun newFile() {
        scriptsPane.tabs.add(ScriptEditor("untitled ${nextUntitled++}"))
        scriptsPane.selectionModel.selectLast()
    }

    @FXML
    private fun openFile() {
        val target = openDialog.showOpenDialog(scriptsPane.scene.window)
        if (target != null) {
            openDialog.initialDirectory = target.parentFile
            tryOpenFile(target)
        }
    }

    private fun tryOpenFile(file: File) {
        val task = object : Task<Unit>() {
            override fun call() {
                Platform.runLater {
                    val editor = ScriptEditor(file, config.experimentalMode.value, config.awakeningMode.value)
                    scriptsPane.tabs.add(editor)
                    scriptsPane.selectionModel.selectLast()
                }
            }
        }
        Thread(task).start()
    }

    @FXML
    private fun closeFile() {
        scriptsPane.tabs.removeAt(scriptsPane.selectionModel.selectedIndex)
    }

    @FXML
    private fun saveFile() {
        performSave()
    }

    @FXML
    private fun saveFileAs() {
        val oldSource = currentEditor().sourceFile
        currentEditor().sourceFile = null

        saveFile()
        if (currentEditor().sourceFile == null)
            currentEditor().sourceFile = oldSource
    }

    private fun performSave() {
        val target = findSaveDestination(currentEditor().sourceFile)
        if (target != null) {
            Files.write(target.toPath(), currentEditor().codeArea.text.toByteArray(StandardCharsets.UTF_8))
            currentEditor().text = target.name
            currentEditor().sourceFile = target
            currentEditor().destFile = null
        }
    }

    private fun findSaveDestination(sourceFile: File?): File? {
        return if (isTextSourceFile(sourceFile))
            sourceFile
        else
            saveDialog.showSaveDialog(scriptsPane.scene.window)
    }

    private fun isTextSourceFile(sourceFile: File?): Boolean {
        return sourceFile != null && !sourceFile.path.endsWith(".cmb")
    }

    @FXML
    private fun compileFile() {
        if (isTextSourceFile(currentEditor().sourceFile))
            performSave()
        performCompile()
    }

    private fun performCompile() {
        val dest = findCompileDestination(currentEditor().destFile)
        if (dest != null) {
            clearConsole()
            compileLabel.isVisible = false
            progressBar.progress = ProgressBar.INDETERMINATE_PROGRESS
            progressBar.isVisible = true
            progressBar.id = null
            val task = object : Task<Unit>() {
                override fun call() {
                    val result = compileFromInMemoryScript(currentEditor().codeArea.text, dest.absolutePath)
                    currentEditor().destFile = dest
                    Platform.runLater {
                        progressBar.progress = 1.0
                        compileLabel.isVisible = true
                        if (result.log.hasErrors()) {
                            console.appendText(result.log.dump())
                            consoleContainer.isVisible = true
                            compileLabel.text = "Build failed."
                            progressBar.id = "failure-bar"
                        } else {
                            compileLabel.text = "Build succeeded."
                            progressBar.id = "success-bar"
                        }
                    }
                }
            }
            Thread(task).start()
        }
    }

    private fun findCompileDestination(destFile: File?): File? {
        return destFile ?: compileDialog.showSaveDialog(scriptsPane.scene.window)
    }

    @FXML
    private fun onDragDropped(event: DragEvent) {
        val db = event.dragboard
        var success = false
        if (db.hasFiles()) {
            success = true
            for (file in db.files)
                tryOpenFile(file)
        }
        event.isDropCompleted = success
        event.consume()
    }

    @FXML
    private fun onDragOver(event: DragEvent) {
        if (event.dragboard.hasFiles()) {
            event.acceptTransferModes(*TransferMode.COPY_OR_MOVE)
        }
        event.consume()
    }

    @FXML
    private fun toggleConsole() {
        consoleContainer.isVisible = !consoleContainer.isVisible
    }

    @FXML
    private fun copyConsole() {
        console.copy()
    }

    @FXML
    private fun clearConsole() {
        console.clear()
    }

    @FXML
    private fun showFindReplaceBar() {
        currentEditor().findReplaceBar.isVisible = true
        currentEditor().findReplaceBar.children[0].requestFocus()
    }

    fun handleCancel(event: KeyEvent) {
        if (event.code == KeyCode.ESCAPE && scriptsPane.tabs.isNotEmpty() && currentEditor().findReplaceBar.isVisible) {
            currentEditor().findReplaceBar.isVisible = false
            event.consume()
        }
    }

    @FXML
    private fun undoScriptEdit() {
        currentEditor().codeArea.undo()
    }

    @FXML
    private fun redoScriptEdit() {
        currentEditor().codeArea.redo()
    }

    @FXML
    private fun cut() {
        currentEditor().codeArea.cut()
    }

    @FXML
    private fun copy() {
        currentEditor().codeArea.copy()
    }

    @FXML
    private fun paste() {
        currentEditor().codeArea.paste()
    }

    @FXML
    private fun delete() {
        currentEditor().codeArea.deleteText(currentEditor().codeArea.selection)
    }

    @FXML
    private fun selectAll() {
        currentEditor().codeArea.selectAll()
    }

    @FXML
    private fun unselectAll() {
        val codeArea = currentEditor().codeArea
        codeArea.selectRange(codeArea.caretPosition, codeArea.caretPosition)
    }

    @FXML
    private fun quit() {
        Platform.exit()
    }

    private fun currentEditor(): ScriptEditor {
        assert(scriptsPane.tabs.isNotEmpty())
        return scriptsPane.selectionModel.selectedItem as ScriptEditor
    }
}