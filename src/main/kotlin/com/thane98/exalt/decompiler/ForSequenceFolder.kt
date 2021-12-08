package com.thane98.exalt.decompiler

import com.thane98.exalt.common.BlockTransformer
import com.thane98.exalt.model.expr.Assignment
import com.thane98.exalt.model.expr.Increment
import com.thane98.exalt.model.stmt.*

class ForSequenceFolder : BlockTransformer() {
    companion object {
        fun fold(stmt: Stmt) {
            val folder = ForSequenceFolder()
            stmt.accept(folder)
        }
    }

    override fun visitBlock(stmt: Block) {
        val contents = stmt.contents
        var i = 0
        while (i < stmt.contents.size - 5) {
            if (isForLoopSequence(contents, i)) {
                val init = (contents[i] as ExprStmt).expr as Assignment
                val step = contents[i + 3] as ExprStmt
                val checkAndBody = contents[i + 5] as If
                val body = checkAndBody.thenPart as Block
                contents.removeAt(i + 1)
                contents.removeAt(i + 2)
                contents.removeAt(i + 3)
                body.contents.removeLast()
                contents[i] = For(init, checkAndBody.condition, step, body)
                body.accept(this)
            } else {
                contents[i].accept(this)
                i++
            }
        }
        while (i < stmt.contents.size) {
            contents[i].accept(this)
            i++
        }
    }

    private fun isForLoopSequence(contents: List<Stmt>, i: Int): Boolean {
        val initStmt = contents[i] as? ExprStmt
        val init = initStmt?.expr as? Assignment
        val bodyGoto = contents[i + 1] as? Goto
        val stepLabel = contents[i + 2] as? Label
        val step = contents[i + 3] as? ExprStmt
        val bodyLabel = contents[i + 4] as? Label
        val checkAndBody = contents[i + 5] as? If
        if (init == null
            || bodyGoto == null
            || stepLabel == null
            || bodyLabel == null
            || checkAndBody == null
            || (step?.expr !is Increment && step?.expr !is Assignment)
        ) {
            return false
        }

        if (bodyGoto.symbol != bodyLabel.symbol
            || checkAndBody.elsePart != null
            || checkAndBody.thenPart !is Block
            || checkAndBody.thenPart.contents.isEmpty()
        ) {
            return false
        }

        val stepGoto = checkAndBody.thenPart.contents.last() as? Goto
        return stepGoto != null && stepGoto.symbol == stepLabel.symbol
    }
}