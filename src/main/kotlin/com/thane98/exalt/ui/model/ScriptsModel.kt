package com.thane98.exalt.ui.model

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

class ScriptsModel {
    val scriptsProperty = SimpleListProperty<ScriptModel>(FXCollections.observableArrayList())
    val scripts: ObservableList<ScriptModel>
        get() = scriptsProperty.value
    val selectedScriptIndexProperty = SimpleIntegerProperty(-1)
    val selectedScriptIndex: Int
        get() = selectedScriptIndexProperty.value
}