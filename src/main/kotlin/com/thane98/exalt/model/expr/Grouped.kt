package com.thane98.exalt.model.expr

import com.thane98.exalt.interfaces.ExprVisitor

data class Grouped(val inner: Expr) : Expr {
    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitGrouped(this)
    }
}
