package com.thane98.exalt.decompiler

import com.thane98.exalt.common.BlockTransformer
import com.thane98.exalt.model.stmt.Block
import com.thane98.exalt.model.stmt.Label
import com.thane98.exalt.model.symbol.LabelSymbol

class LabelResolver private constructor(
    private val labels: Map<Int, List<LabelSymbol>>,
    private val statementAddressTracker: StatementAddressTracker,
) : BlockTransformer() {
    private var resolvedLabelCount = 0

    companion object {
        fun resolve(
            stmt: Block, labels: List<LabelSymbol>,
            statementAddressTracker: StatementAddressTracker
        ) {
            if (labels.isNotEmpty()) {
                val groupedLabels = labels.sortedBy { it.address!! }.groupBy { it.address!! }
                val resolver = LabelResolver(groupedLabels, statementAddressTracker)
                stmt.accept(resolver)
                if (resolver.resolvedLabelCount != labels.size) {
                    throw IllegalStateException("Resolved ${resolver.resolvedLabelCount} labels but wanted ${labels.size}")
                }
            }
        }
    }

    override fun visitBlock(stmt: Block) {
        val contents = stmt.contents
        var i = 0
        while (i < contents.size) {
            val address = statementAddressTracker.lookup(contents[i])
            if (address in labels) {
                contents.addAll(i, labels[address]!!.map { Label(it) })
                resolvedLabelCount += labels[address]!!.size
                i++
            }
            contents[i].accept(this)
            i++
        }
    }
}