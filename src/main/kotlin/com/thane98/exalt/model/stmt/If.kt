package com.thane98.exalt.model.stmt

import com.thane98.exalt.interfaces.StmtVisitor
import com.thane98.exalt.model.expr.Expr

class If(val condition: Expr, val thenPart: Stmt, val elsePart: Stmt?): Stmt {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitIf(this)
    }

    override fun toString(): String {
        return "If(condition=$condition, thenPart=$thenPart, elsePart=$elsePart)"
    }
}
