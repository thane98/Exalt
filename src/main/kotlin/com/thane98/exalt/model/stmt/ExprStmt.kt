package com.thane98.exalt.model.stmt

import com.thane98.exalt.interfaces.StmtVisitor
import com.thane98.exalt.model.expr.Expr

class ExprStmt(val expr: Expr): Stmt {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitExprStmt(this)
    }

    override fun toString(): String {
        return "ExprStmt(expr=$expr)"
    }
}
