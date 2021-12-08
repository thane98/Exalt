package com.thane98.exalt.model.stmt

import com.thane98.exalt.interfaces.StmtVisitor

class Yield: Stmt {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitYield(this)
    }

    override fun toString(): String {
        return "Yield()"
    }
}