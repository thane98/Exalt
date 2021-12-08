package com.thane98.exalt.model.stmt

import com.thane98.exalt.interfaces.StmtVisitor
import com.thane98.exalt.model.expr.Expr

class Return(val value: Expr) : Stmt {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitReturn(this)
    }

    override fun toString(): String {
        return "Return(value=$value)"
    }
}