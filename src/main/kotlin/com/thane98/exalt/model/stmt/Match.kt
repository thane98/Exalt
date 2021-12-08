package com.thane98.exalt.model.stmt

import com.thane98.exalt.interfaces.StmtVisitor
import com.thane98.exalt.model.expr.Expr

class Match(val switch: Expr, val cases: MutableList<Case>, var default: Stmt?): Stmt {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitMatch(this)
    }

    override fun toString(): String {
        return "Match(switch=$switch, cases=$cases, default=$default)"
    }
}
