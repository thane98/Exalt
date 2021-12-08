package com.thane98.exalt.model.expr

import com.thane98.exalt.interfaces.ExprVisitor
import com.thane98.exalt.model.Operator

data class Increment(val op: Operator, val operand: Ref, val isPrefix: Boolean): Expr {
    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitIncrement(this)
    }
}