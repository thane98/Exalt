package com.thane98.exalt.decompiler

import com.thane98.exalt.model.symbol.LabelSymbol

class LabelVendor {
    private val labels: MutableMap<Int, LabelSymbol> = mutableMapOf()
    private var nextLabel = 0

    fun purge(address: Int) {
        labels.remove(address)
    }

    fun labelAt(address: Int): LabelSymbol {
        return if (address in labels) {
            labels[address]!!
        } else {
            val symbol = LabelSymbol("l$nextLabel", address)
            nextLabel++
            labels[address] = symbol
            symbol
        }
    }

    fun allLabels(): List<LabelSymbol> {
        return labels.values.toList()
    }
}