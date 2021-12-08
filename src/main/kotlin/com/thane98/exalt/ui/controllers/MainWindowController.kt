package com.thane98.exalt.ui.controllers

import com.thane98.exalt.error.AggregatedCompilerError
import com.thane98.exalt.model.Game
import com.thane98.exalt.ui.controls.ErrorTableView
import com.thane98.exalt.ui.misc.ConfigFormFactory
import com.thane98.exalt.ui.misc.DialogUtils
import com.thane98.exalt.ui.model.CompileResultModel
import com.thane98.exalt.ui.model.ConfigModel
import com.thane98.exalt.ui.model.ScriptModel
import com.thane98.exalt.ui.model.ScriptsModel
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import java.net.URL
import java.util.*

class MainWindowController : Initializable {
    @FXML
    private lateinit var root: VBox

    @FXML
    private lateinit var showToolBarMenuItem: CheckMenuItem

    @FXML
    private lateinit var showStatusBarMenuItem: CheckMenuItem

    @FXML
    private lateinit var showConsoleMenuItem: CheckMenuItem

    @FXML
    private lateinit var toolBar: ToolBar

    @FXML
    private lateinit var tabPane: TabPane

    @FXML
    private lateinit var splitPane: SplitPane

    @FXML
    private lateinit var gameComboBox: ComboBox<Game>

    @FXML
    private lateinit var statusMessage: Label

    private lateinit var errorTableView: ErrorTableView

    lateinit var scriptsModel: ScriptsModel
    private lateinit var scriptsTabPaneController: ScriptsTabPaneController

    private val openFileChooser = FileChooser()
    val configModel = ConfigModel.loadFromPreferences()

    private val compileResultModel = CompileResultModel()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        openFileChooser.extensionFilters.add(
            FileChooser.ExtensionFilter("Exalt Scripts", "*.cmb", "*.exl")
        )

        gameComboBox.items = FXCollections.observableList(Game.values().toList())
        gameComboBox.valueProperty().bindBidirectional(configModel.gameProperty)

        showStatusBarMenuItem.selectedProperty().bindBidirectional(configModel.showStatusBarProperty)
        showToolBarMenuItem.selectedProperty().bindBidirectional(configModel.showToolBarProperty)
        toolBar.visibleProperty().bind(showToolBarMenuItem.selectedProperty())
        toolBar.managedProperty().bind(toolBar.visibleProperty())
        showConsoleMenuItem.selectedProperty().bindBidirectional(configModel.showConsoleProperty)

        scriptsModel = ScriptsModel()
        scriptsTabPaneController = ScriptsTabPaneController(tabPane, scriptsModel)

        errorTableView = ErrorTableView(compileResultModel)
    }

    fun prepareForExit() {
        scriptsTabPaneController.prepareForExit()
        configModel.saveToPreferences()
    }

    @FXML
    private fun onOpenOptions() {
        val window = root.scene.window
        val stage = ConfigFormFactory.createForm(configModel, window)
        stage.show()
    }

    @FXML
    private fun updateErrorTableVisibilityAfterRun() {
        if (compileResultModel.lastUpdateHadErrors) {
            if (!splitPane.items.contains(errorTableView)) {
                splitPane.items.add(errorTableView)
                splitPane.setDividerPosition(0, 0.6)
            }
        }
    }

    @FXML
    private fun onToggleConsole() {
        if (!splitPane.items.contains(errorTableView)) {
            splitPane.items.add(errorTableView)
        } else {
            splitPane.items.remove(errorTableView)
        }
    }

    @FXML
    private fun onDragDropped(event: DragEvent) {
        val db = event.dragboard
        var success = false
        if (db.hasFiles()) {
            success = true
            try {
                db.files.forEach { scriptsTabPaneController.openScriptInNewEditor(it.absolutePath, gameComboBox.value) }
            } catch (ex: Exception) {
                DialogUtils.createErrorDialog(ex, "Error Compiling File").showAndWait()
            }
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
    private fun onNewFile() {
        scriptsModel.scripts.add(ScriptModel())
    }

    @FXML
    private fun onOpenFile() {
        val choice = openFileChooser.showOpenDialog(root.scene.window)
        if (choice != null) {
            try {
                scriptsTabPaneController.openScriptInNewEditor(choice.absolutePath, gameComboBox.value)
            } catch (ex: Exception) {
                DialogUtils.createErrorDialog(ex, "Error Compiling File").showAndWait()
            }
        }
    }

    @FXML
    private fun onCompileFile() {
        try {
            scriptsTabPaneController.getCurrentScriptEditorController()?.compileAndSaveScript()
            compileResultModel.update(listOf())
            statusMessage.text = "Build succeeded (${Date()})"
        } catch (aggregatedError: AggregatedCompilerError) {
            compileResultModel.update(aggregatedError.errors())
        } catch (ex: Exception) {
            DialogUtils.createErrorDialog(ex, "Error Compiling File").showAndWait()
        }
        updateErrorTableVisibilityAfterRun()
    }

    @FXML
    private fun onCompileFileAs() {
        try {
            scriptsTabPaneController.getCurrentScriptEditorController()?.compileAndSaveScriptUnderNewName()
            compileResultModel.update(listOf())
            statusMessage.text = "Build succeeded! (${Date()})"
        } catch (aggregatedError: AggregatedCompilerError) {
            compileResultModel.update(aggregatedError.errors())
        } catch (ex: Exception) {
            DialogUtils.createErrorDialog(ex, "Error Compiling File").showAndWait()
        }
        updateErrorTableVisibilityAfterRun()
    }

    @FXML
    private fun onSaveFile() {
        try {
            scriptsTabPaneController.getCurrentScriptEditorController()?.saveScript()
        } catch (ex: Exception) {
            DialogUtils.createErrorDialog(ex, "Error Compiling File").showAndWait()
        }
    }

    @FXML
    private fun onSaveFileAs() {
        try {
            scriptsTabPaneController.getCurrentScriptEditorController()?.saveScriptUnderNewName()
        } catch (ex: Exception) {
            DialogUtils.createErrorDialog(ex, "Error Compiling File").showAndWait()
        }
    }

    @FXML
    private fun onQuit() {
        Platform.exit()
    }

    @FXML
    private fun onUndo() {
        scriptsTabPaneController.getCurrentScriptEditorController()?.undo()
    }

    @FXML
    private fun onRedo() {
        scriptsTabPaneController.getCurrentScriptEditorController()?.redo()
    }

    @FXML
    private fun onCut() {
        scriptsTabPaneController.getCurrentScriptEditorController()?.cut()
    }

    @FXML
    private fun onCopy() {
        scriptsTabPaneController.getCurrentScriptEditorController()?.copy()
    }

    @FXML
    private fun onPaste() {
        scriptsTabPaneController.getCurrentScriptEditorController()?.paste()
    }

    @FXML
    private fun onDelete() {
        scriptsTabPaneController.getCurrentScriptEditorController()?.delete()
    }

    @FXML
    private fun onSelectAll() {
        scriptsTabPaneController.getCurrentScriptEditorController()?.selectAll()
    }

    @FXML
    private fun onUnselectAll() {
        scriptsTabPaneController.getCurrentScriptEditorController()?.unselect()
    }

    @FXML
    private fun onShowFindReplaceBar() {
        scriptsTabPaneController.getCurrentScriptEditorController()?.showFindReplaceBar()
    }
}