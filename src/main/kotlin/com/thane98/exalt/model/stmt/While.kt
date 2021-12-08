package com.thane98.exalt.model.stmt

import com.thane98.exalt.interfaces.StmtVisitor
import com.thane98.exalt.model.expr.Expr

class While(val condition: Expr, val body: Stmt): Stmt {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitWhile(this)
    }

    override fun toString(): String {
        return "While(condition=$condition, body=$body)"
    }
}
