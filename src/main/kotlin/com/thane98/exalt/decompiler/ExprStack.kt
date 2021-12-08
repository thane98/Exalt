package com.thane98.exalt.decompiler

import com.thane98.exalt.model.expr.Expr

class ExprStack {
    private val container = ArrayDeque<Expr>()

    fun pop(): Expr {
        return container.removeLast()
    }

    fun push(expr: Expr) {
        container.addLast(expr)
    }

    fun peek(): Expr {
        return container.last()
    }

    fun isEmpty(): Boolean {
        return container.isEmpty()
    }

    fun popFunctionArguments(count: Int): List<Expr> {
        val args = mutableListOf<Expr>()
        for (i in 0 until count) {
            args.add(pop())
        }
        return args.reversed()
    }
}