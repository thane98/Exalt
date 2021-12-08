package com.thane98.exalt.ui.controls

import com.thane98.exalt.error.CompilerError
import com.thane98.exalt.ui.model.CompileResultModel
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView

class ErrorTableView(
    compileResultModel: CompileResultModel
) : TableView<CompilerError>() {
    init {
        val fileColumn = TableColumn<CompilerError, String>()
        fileColumn.setCellValueFactory { SimpleStringProperty(it.value.position.file) }
        fileColumn.text = "File"
        fileColumn.prefWidth = 200.0
        val lineColumn = TableColumn<CompilerError, Number>()
        lineColumn.setCellValueFactory { SimpleIntegerProperty(it.value.position.lineNumber + 1) }
        lineColumn.text = "Line"
        lineColumn.prefWidth = 100.0
        val indexColumn = TableColumn<CompilerError, Number>()
        indexColumn.setCellValueFactory { SimpleIntegerProperty(it.value.position.index) }
        indexColumn.text = "Index"
        indexColumn.prefWidth = 100.0
        val messageColumn = TableColumn<CompilerError, String>()
        messageColumn.setCellValueFactory { SimpleStringProperty(it.value.message) }
        messageColumn.text = "Message"
        messageColumn.prefWidth = 300.0
        messageColumn.setCellFactory { ErrorMessageTableViewCell() }
        this.columns.addAll(fileColumn, lineColumn, indexColumn, messageColumn)

        this.itemsProperty().bind(compileResultModel.errorsProperty)

        this.prefHeight = 200.0
    }
}