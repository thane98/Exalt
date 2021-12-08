package com.thane98.exalt.model.expr

import com.thane98.exalt.interfaces.ExprVisitor
import com.thane98.exalt.model.symbol.FunctionSymbol

data class Funcall(val symbol: FunctionSymbol, val args: List<Expr>): Expr {
    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitFuncall(this)
    }
}
