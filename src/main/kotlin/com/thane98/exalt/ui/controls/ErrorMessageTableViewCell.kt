package com.thane98.exalt.ui.controls

import com.thane98.exalt.error.CompilerError
import javafx.scene.control.TableCell
import javafx.scene.text.Text

class ErrorMessageTableViewCell : TableCell<CompilerError, String>() {
    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)
        val text = Text()
        text.text = item
        text.wrappingWidth = 300.0
        graphic = text
    }
}