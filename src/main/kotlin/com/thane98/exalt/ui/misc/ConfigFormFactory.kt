package com.thane98.exalt.ui.misc

import com.thane98.exalt.ui.Main
import com.thane98.exalt.ui.controllers.ConfigFormController
import com.thane98.exalt.ui.model.ConfigModel
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.GridPane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window

class ConfigFormFactory {
    companion object {
        fun createForm(model: ConfigModel, owner: Window): Stage {
            // Load the form
            val loader = FXMLLoader()
            val form = loader.load<GridPane>(Main::class.java.getResourceAsStream("ConfigForm.fxml"))
            val controller = loader.getController<ConfigFormController>()
            controller?.setModel(model)

            // Set up the stage
            val stage = Stage()
            stage.initOwner(owner)
            stage.initModality(Modality.WINDOW_MODAL)
            stage.isResizable = false
            stage.scene = Scene(form)
            stage.title = "Exalt - Options"
            StyleUtils.apply(stage.scene)
            return stage
        }
    }
}