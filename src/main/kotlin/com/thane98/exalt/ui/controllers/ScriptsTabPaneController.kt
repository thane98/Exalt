package com.thane98.exalt.ui.controllers

import com.thane98.exalt.model.Game
import com.thane98.exalt.ui.misc.FileUtils
import com.thane98.exalt.ui.misc.ScriptEditorFactory
import com.thane98.exalt.ui.model.ScriptModel
import com.thane98.exalt.ui.model.ScriptsModel
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import java.net.URL
import java.util.*

class ScriptsTabPaneController(
    private val tabPane: TabPane,
    private val scriptsModel: ScriptsModel
) {
    private val scriptEditorFactory = ScriptEditorFactory()
    private var processModelUpdates = true

    init {
        tabPane.tabs.clear()
        tabPane.tabs.addAll(scriptsModel.scripts.map { scriptEditorFactory.createEditor(it) })
        scriptsModel.selectedScriptIndexProperty.bind(tabPane.selectionModel.selectedIndexProperty())
        scriptsModel.scriptsProperty.addListener(ListChangeListener { updateTabsBasedOnUnderlyingModelChange(it) })
    }

    private fun updateTabsBasedOnUnderlyingModelChange(change: ListChangeListener.Change<out ScriptModel>) {
        if (!processModelUpdates) {
            return
        }
        while(change.next()) {
            if (change.wasAdded()) {
                tabPane.tabs.addAll(change.from, change.addedSubList.map { createEditor(it) })
                tabPane.selectionModel.select(change.to - 1)
            } else if (change.wasRemoved()) {
                tabPane.tabs.remove(change.from, change.to)
            } else {
                throw IllegalStateException("Unsupported operation on ScriptsTabPaneController tabs.")
            }
        }
    }

    private fun createEditor(model: ScriptModel): Tab {
        val editor = scriptEditorFactory.createEditor(model)
        editor.setOnClosed {
            // The tab was already deleted by the time we get this.
            // We only need to make sure the change is reflected in the model.
            processModelUpdates = false
            scriptsModel.scripts.remove(model)
            processModelUpdates = true
        }
        return editor
    }

    fun openScriptInNewEditor(path: String, game: Game) {
        scriptsModel.scripts.add(ScriptModel.fromFile(path, game))
    }

    fun getCurrentScriptEditorController(): ScriptEditorController? {
        return if (scriptsModel.selectedScriptIndex == -1) {
            null
        } else {
            tabPane.tabs[scriptsModel.selectedScriptIndex].properties["controller"] as ScriptEditorController
        }
    }

    fun prepareForExit() {
        tabPane.tabs.forEach {
            val controller = it.properties["controller"] as ScriptEditorController
            controller.terminateAnalyzers()
        }
    }
}