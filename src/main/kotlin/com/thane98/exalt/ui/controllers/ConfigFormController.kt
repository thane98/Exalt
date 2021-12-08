package com.thane98.exalt.ui.controllers

import com.thane98.exalt.ui.model.ConfigModel
import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.Spinner

class ConfigFormController {
    @FXML
    private lateinit var translateFunctionNamesCheckBox: CheckBox
    @FXML
    private lateinit var enableServerCheckBox: CheckBox
    @FXML
    private lateinit var serverPortSpinner: Spinner<Int>

    fun setModel(model: ConfigModel) {
        translateFunctionNamesCheckBox.selectedProperty().bindBidirectional(model.translateFunctionNamesProperty)
        enableServerCheckBox.selectedProperty().bindBidirectional(model.enableScriptServerProperty)
        serverPortSpinner.valueFactory.valueProperty().bindBidirectional(model.scriptServerPortProperty.asObject())
    }
}