package com.thane98.exalt.model.stmt

import com.thane98.exalt.interfaces.StmtVisitor
import com.thane98.exalt.model.expr.Assignment
import com.thane98.exalt.model.expr.Expr

class For(
    val init: Assignment,
    val check: Expr,
    val step: ExprStmt,
    val body: Stmt,
) : Stmt {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitFor(this)
    }

    override fun toString(): String {
        return "For(init=$init, check=$check, step=$step, body=$body)"
    }
}
