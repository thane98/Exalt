package com.thane98.exalt.decompiler

import com.thane98.exalt.common.BlockTransformer
import com.thane98.exalt.model.stmt.Block
import com.thane98.exalt.model.stmt.Goto
import com.thane98.exalt.model.stmt.Stmt
import com.thane98.exalt.model.symbol.LabelSymbol

class LabelReferenceCounter private constructor() : BlockTransformer() {
    private val labelReferenceCounts = mutableMapOf<LabelSymbol, Int>()

    companion object {
        fun countReferences(stmt: Stmt): Map<LabelSymbol, Int> {
            val counter = LabelReferenceCounter()
            stmt.accept(counter)
            return counter.labelReferenceCounts
        }
    }

    override fun visitBlock(stmt: Block) {
        for (line in stmt.contents) {
            line.accept(this)
        }
    }

    override fun visitGoto(stmt: Goto) {
        val count = labelReferenceCounts.getOrDefault(stmt.symbol!!, 0)
        labelReferenceCounts[stmt.symbol!!] = count + 1
    }
}