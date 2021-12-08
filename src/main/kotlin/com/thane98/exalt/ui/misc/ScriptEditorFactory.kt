package com.thane98.exalt.ui.misc

import com.thane98.exalt.ui.Main
import com.thane98.exalt.ui.controllers.ScriptEditorController
import com.thane98.exalt.ui.model.ScriptModel
import javafx.fxml.FXMLLoader
import javafx.scene.control.Tab

class ScriptEditorFactory {
    fun createEditor(model: ScriptModel): Tab {
        val controller = ScriptEditorController(model)
        val loader = FXMLLoader()
        loader.setController(controller)
        val tab = loader.load<Tab>(Main::class.java.getResourceAsStream("ScriptEditor.fxml"))
        tab.properties["controller"] = controller
        return tab
    }
}