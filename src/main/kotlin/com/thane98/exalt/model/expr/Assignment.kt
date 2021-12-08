package com.thane98.exalt.model.expr

import com.thane98.exalt.interfaces.ExprVisitor
import com.thane98.exalt.model.Operator

data class Assignment(val left: Ref, val right: Expr, val op: Operator): Expr {
    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitAssignment(this)
    }
}
