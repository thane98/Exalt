package com.thane98.exalt.model.expr

import com.thane98.exalt.interfaces.ExprVisitor

interface Expr {
    fun <T> accept(visitor: ExprVisitor<T>): T
}