package com.thane98.exalt.ui.misc

import com.thane98.exalt.ui.Main
import com.thane98.exalt.ui.controllers.ErrorDialogController
import javafx.fxml.FXMLLoader
import javafx.stage.Modality
import javafx.stage.Stage

class DialogUtils {
    companion object {
        fun createErrorDialog(throwable: Throwable, headerText: String): Stage {
            val loader = FXMLLoader()
            loader.setController(ErrorDialogController(throwable, headerText))
            val stage = loader.load<Stage>(Main::class.java.getResourceAsStream("ErrorDialog.fxml"))
            stage.initModality(Modality.APPLICATION_MODAL)
            StyleUtils.apply(stage.scene)
            return stage
        }
    }
}