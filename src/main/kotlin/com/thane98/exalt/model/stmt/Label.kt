package com.thane98.exalt.model.stmt

import com.thane98.exalt.interfaces.StmtVisitor
import com.thane98.exalt.model.symbol.LabelSymbol

class Label(var symbol: LabelSymbol): Stmt {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitLabel(this)
    }

    override fun toString(): String {
        return "Label(symbol=$symbol)"
    }
}
