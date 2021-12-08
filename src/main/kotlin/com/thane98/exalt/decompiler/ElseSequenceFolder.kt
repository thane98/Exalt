package com.thane98.exalt.decompiler

import com.thane98.exalt.common.BlockTransformer
import com.thane98.exalt.model.stmt.*

class ElseSequenceFolder private constructor() : BlockTransformer() {
    companion object {
        fun fold(stmt: Stmt) {
            val folder = ElseSequenceFolder()
            stmt.accept(folder)
        }
    }

    override fun visitBlock(stmt: Block) {
        val contents = stmt.contents
        var i = 0
        while (i < contents.size) {
            val line = contents[i]
            line.accept(this)
            if (isIfElseCandidate(contents, i)) {
                val cond = line as If
                val thenPart = cond.thenPart as Block
                val goto = thenPart.contents.last() as Goto

                // Looks like if/else, but maybe the goto points to somewhere we don't know about.
                val elsePart = mutableListOf<Stmt>()
                var success = false
                var j = i + 1
                while (j < contents.size) {
                    if (contents[j] is Label) {
                        val label = contents[j] as Label
                        if (label.symbol == goto.symbol) {
                            success = true
                            break
                        }
                    } else {
                        elsePart.add(contents[j])
                        j++
                    }
                }
                if (success) {
                    thenPart.contents.removeLast()
                    contents[i] = If(line.condition, line.thenPart, Block(elsePart))
                    contents.subList(i + 1, j).clear()
                }
            }
            i++
        }
    }

    private fun isIfElseCandidate(contents: List<Stmt>, i: Int): Boolean {
        val line = contents[i]
        if (line !is If || line.elsePart != null || line.thenPart !is Block) {
            return false
        }

        val thenPart = line.thenPart
        return thenPart.contents.isNotEmpty() && thenPart.contents.last() is Goto
    }
}