package com.thane98.exalt.model.expr

import com.thane98.exalt.interfaces.ExprVisitor
import com.thane98.exalt.model.Operator

data class Unary(val op: Operator, val operand: Expr): Expr {
    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitUnary(this)
    }
}
