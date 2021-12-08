package com.thane98.exalt.ui.model

import com.thane98.exalt.error.CompilerError
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList


class CompileResultModel {
    val errorsProperty = SimpleListProperty<CompilerError>(FXCollections.observableArrayList())
    val errors: ObservableList<CompilerError>
        get() = errorsProperty.value
    val messageProperty = SimpleStringProperty()
    var message: String?
        get() = messageProperty.value
        set(value) { messageProperty.value = value }
    val lastUpdateHadErrors: Boolean
        get() = errors.isNotEmpty()

    fun update(errors: List<CompilerError>) {
        this.errors.clear()
        this.errors.addAll(errors)
        this.message = if (errors.isEmpty()) "Build Succeeded" else "Build Failed"
    }
}