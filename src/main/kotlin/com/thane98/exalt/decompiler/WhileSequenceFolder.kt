package com.thane98.exalt.decompiler

import com.thane98.exalt.common.BlockTransformer
import com.thane98.exalt.model.stmt.*

class WhileSequenceFolder private constructor() : BlockTransformer() {
    companion object {
        fun fold(stmt: Stmt) {
            val folder = WhileSequenceFolder()
            stmt.accept(folder)
        }
    }

    override fun visitBlock(stmt: Block) {
        val contents = stmt.contents
        var i = 0
        while (i < contents.size - 1) {
            if (isWhileLoopSequence(contents, i)) {
                val checkAndBody = contents[i + 1] as If
                val body = checkAndBody.thenPart as Block
                body.contents.removeLast()
                contents[i + 1] = While(checkAndBody.condition, body)
                body.accept(this)
                i += 2
            } else {
                contents[i].accept(this)
                i++
            }
        }
        if (i < contents.size) {
            contents[i].accept(this)
        }
    }

    private fun isWhileLoopSequence(contents: List<Stmt>, i: Int): Boolean {
        // Does it look like a while loop sequence?
        val label = contents[i] as? Label
        val checkAndBody = contents[i + 1] as? If
        if (label == null || checkAndBody == null) {
            return false
        }

        // Check the "loop" body to confirm.
        val thenPart = checkAndBody.thenPart
        if (checkAndBody.elsePart != null || thenPart !is Block || thenPart.contents.isEmpty()) {
            return false
        }

        val goto = thenPart.contents.last() as? Goto
        return goto != null && goto.symbol == label.symbol
    }
}