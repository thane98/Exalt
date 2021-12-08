package com.thane98.exalt.model.expr

import com.thane98.exalt.interfaces.ExprVisitor
import com.thane98.exalt.model.symbol.VarSymbol

data class ArrayRef(override val symbol: VarSymbol, val index: Expr, override var isPointer: Boolean = false): Ref {
    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitArrayRef(this)
    }
}
