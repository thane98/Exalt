package com.thane98.exalt.ui.misc

import com.thane98.exalt.ui.model.ScriptModel
import com.thane98.exalt.ui.model.ScriptOpenRequest
import com.thane98.exalt.ui.model.ScriptsModel
import javafx.application.Platform
import javafx.stage.Stage

class ScriptOpenRequestProcessor(
    private val model: ScriptsModel,
    private val stage: Stage,
) {
    fun processRequest(scriptOpenRequest: ScriptOpenRequest) {
        Platform.runLater {
            openScriptAndUpdateSavePath(scriptOpenRequest)
            stage.isAlwaysOnTop = true
            stage.isAlwaysOnTop = false
            stage.requestFocus()
        }
    }

    private fun openScriptAndUpdateSavePath(scriptOpenRequest: ScriptOpenRequest) {
        // TODO: Select the correct tab?
        for (i in model.scripts.indices) {
            val script = model.scripts[i]
            if (script.compiledFilePath == scriptOpenRequest.savePath) {
                return
            }
        }
        val scriptModel = ScriptModel.fromFile(scriptOpenRequest.openPath, scriptOpenRequest.game)
        scriptModel.compiledFilePath = scriptOpenRequest.savePath
        model.scripts.add(scriptModel)
    }
}