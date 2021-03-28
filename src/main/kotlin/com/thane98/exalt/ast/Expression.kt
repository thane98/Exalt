package com.thane98.exalt.ast

import com.thane98.exalt.common.TokenType

abstract class Expr {
    abstract fun <T> accept(visitor: ExprVisitor<T>): T
}

interface ExprVisitor<T> {
    fun visitLiteral(expr: Literal): T
    fun visitUnaryExpr(expr: UnaryExpr): T
    fun visitBinaryExpr(expr: BinaryExpr): T
    fun visitIncrement(expr: Increment): T
    fun visitGroupedExpr(expr: GroupedExpr): T
    fun visitFuncall(expr: Funcall): T
    fun visitVarRef(expr: VarRef): T
    fun visitArrayRef(expr: ArrayRef): T
}

class Literal(val value: Any): Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitLiteral(this)
    }
}

class UnaryExpr(val op: TokenType, val expr: Expr): Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitUnaryExpr(this)
    }
}

class BinaryExpr(val lhs: Expr, val op: TokenType, val rhs: Expr): Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitBinaryExpr(this)
    }
}

class Increment(val op: TokenType, val target: Ref, val isPrefix: Boolean): Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitIncrement(this)
    }

}

class GroupedExpr(val expr: Expr): Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitGroupedExpr(this)
    }
}

class Funcall(val target: String, val args: List<Expr>, var callID: Int = -1): Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitFuncall(this)
    }

    val isExlcall: Boolean
        get() = target == "__exlcall__"

    val isFormat: Boolean
        get() = target == "format"

    val isLocalCall: Boolean
        get() = callID != -1
}

abstract class Ref(var isPointer: Boolean = false): Expr()

class VarRef(val symbol: VarSymbol, isPointer: Boolean = false): Ref(isPointer) {
    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitVarRef(this)
    }
}

class ArrayRef(val symbol: VarSymbol, val index: Expr, isPointer: Boolean = false): Ref(isPointer) {
    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitArrayRef(this)
    }
}