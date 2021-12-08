package com.thane98.exalt.interfaces

import com.thane98.exalt.model.expr.*

interface ExprVisitor<T> {
    fun visitLiteral(expr: Literal): T
    fun visitGrouped(expr: Grouped): T
    fun visitVarRef(expr: VarRef): T
    fun visitArrayRef(expr: ArrayRef): T
    fun visitPointerRef(expr: PointerRef): T
    fun visitUnary(expr: Unary): T
    fun visitBinary(expr: Binary): T
    fun visitFuncall(expr: Funcall): T
    fun visitAssignment(expr: Assignment): T
    fun visitIncrement(expr: Increment): T
}