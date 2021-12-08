package com.thane98.exalt.model.stmt

import com.thane98.exalt.interfaces.StmtVisitor

class Block(val contents: MutableList<Stmt> = mutableListOf()): Stmt {
    companion object {
        fun empty(): Block {
            return Block(mutableListOf())
        }
    }

    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitBlock(this)
    }

    override fun toString(): String {
        return "Block(contents=$contents)"
    }
}
