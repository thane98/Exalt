package com.thane98.exalt.compiler

import com.thane98.exalt.common.SymbolTable
import com.thane98.exalt.model.stmt.Goto
import com.thane98.exalt.model.stmt.Label
import com.thane98.exalt.model.symbol.LabelSymbol

class LabelTracker(private val symbolTable: SymbolTable) {
    private val unresolvedGotos = mutableMapOf<String, MutableList<Goto>>()
    private val knownLabelSymbols = mutableMapOf<String, LabelSymbol>()

    fun addGoto(goto: Goto, labelName: String) {
        if (labelName in knownLabelSymbols) {
            goto.symbol = knownLabelSymbols[labelName]!!
        } else {
            val bucket = unresolvedGotos.getOrPut(labelName) { mutableListOf() }
            bucket.add(goto)
        }
    }

    fun addLabel(label: Label) {
        val symbol = label.symbol
        knownLabelSymbols[symbol.name] = symbol
        if (symbol.name in unresolvedGotos) {
            for (goto in unresolvedGotos.remove(symbol.name)!!) {
                goto.symbol = symbol
            }
        }
    }

    fun hasUnresolvedGotos(): Boolean {
        return unresolvedGotos.isNotEmpty()
    }
}