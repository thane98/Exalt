package com.thane98.exalt.decompiler

import com.thane98.exalt.model.stmt.Block
import com.thane98.exalt.model.stmt.Stmt

class BlockStack {
    private val container = ArrayDeque<Block>()

    init {
        push()
    }

    fun line(stmt: Stmt) {
        peek().contents.add(stmt)
    }

    fun lineCount(): Int {
        return peek().contents.size
    }

    fun push(block: Block = Block.empty()) {
        container.addLast(block)
    }

    fun pop(): Block {
        return container.removeLast()
    }

    fun peek(): Block {
        return container.last()
    }

    fun peekLine(): Stmt {
        return peek().contents.last()
    }
}