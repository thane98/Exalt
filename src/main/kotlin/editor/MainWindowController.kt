package editor

import compiler.compile
import compiler.compileFromInMemoryScript
import javafx.application.Platform
import javafx.beans.Observable
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.stage.FileChooser
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


class MainWindowController : Initializable {
    @FXML
    private lateinit var scriptsPane: TabPane
    @FXML
    private lateinit var toolBarSpacer: Pane
    @FXML
    private lateinit var statusBarSpacer: Pane
    @FXML
    private lateinit var experimentalModeItem: CheckMenuItem
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
    private lateinit var console: TextArea
    @FXML
    private lateinit var compileLabel: Label
    @FXML
    private lateinit var progressBar: ProgressBar

    private var nextUntitled = 0
    private val config = Config()

    private val openDialog = FileChooser()
    private val saveDialog = FileChooser()
    private val compileDialog = FileChooser()


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // Configure resizing for console and spacers.
        HBox.setHgrow(toolBarSpacer, Priority.ALWAYS)
        HBox.setHgrow(statusBarSpacer, Priority.ALWAYS)
        console.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_PREF_SIZE)

        scriptsPane.tabs.addListener { _: Observable -> toggleActions() }
        experimentalModeItem.isSelected = config.experimentalMode.value
        config.experimentalMode.bind(experimentalModeItem.selectedProperty())
        toggleActions()
        setupFileDialogs()
    }

    private fun setupFileDialogs() {
        openDialog.title = "Open File"
        openDialog.extensionFilters.addAll(
            FileChooser.ExtensionFilter("Compiled Script", "*.cmb"),
            FileChooser.ExtensionFilter("Exalt Script", "*exl"),
            FileChooser.ExtensionFilter("All Supported Types", "*.cmb", "*.exl")
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
                    scriptsPane.tabs.add(ScriptEditor(file, config.experimentalMode.value))
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
                            console.isVisible = true
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
        console.isVisible = !console.isVisible
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