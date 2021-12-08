package com.thane98.exalt.decompiler

import com.thane98.exalt.common.BlockTransformer
import com.thane98.exalt.model.stmt.Block
import com.thane98.exalt.model.stmt.Label
import com.thane98.exalt.model.stmt.Stmt
import com.thane98.exalt.model.symbol.LabelSymbol

class LabelPruner private constructor(private val referenceCounts: Map<LabelSymbol, Int>): BlockTransformer() {
    companion object {
        fun prune(stmt: Stmt) {
            val pruner = LabelPruner(LabelReferenceCounter.countReferences(stmt))
            stmt.accept(pruner)
        }
    }

    override fun visitBlock(stmt: Block) {
        val contents = stmt.contents
        var i = 0
        while (i < contents.size) {
            val cur = contents[i]
            if (cur is Label && referenceCounts.getOrDefault(cur.symbol, 0) == 0) {
                contents.removeAt(i)
            } else {
                contents[i].accept(this)
                i++
            }
        }
    }
}