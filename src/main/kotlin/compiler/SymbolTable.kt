package compiler

import ast.LabelSymbol
import ast.Symbol

class SymbolTable {
    private val levels = mutableListOf<HashMap<String, Symbol>>()

    init {
        enterScope()
    }

    fun enterScope() {
        levels.add(hashMapOf())
    }

    fun exitScope() {
        levels.removeAt(levels.lastIndex)
    }

    fun define(anchor: Token, symbol: Symbol) {
        checkForRedefinition(anchor, symbol.name, levels.last())
        levels.last()[symbol.name] = symbol
    }

    fun defineLabel(anchor: Token, symbol: LabelSymbol) {
        assert(levels.size >= 2)
        checkForRedefinition(anchor, symbol.name, levels[1])
        levels[1][symbol.name] = symbol
    }

    private fun checkForRedefinition(anchor: Token, name: String, level: HashMap<String, Symbol>) {
        if (level.containsKey(name))
            throw CompileError("Redefinition of symbol $name", anchor.pos)
    }

    fun get(anchor: Token, name: String): Symbol {
        return search(name) ?: throw CompileError("Undefined symbol: $name.", anchor.pos)
    }

    fun defined(name: String): Boolean {
        return search(name) != null
    }

    private fun search(name: String): Symbol? {
        for (i in levels.lastIndex downTo 0) {
            val sym = levels[i][name]
            if (sym != null)
                return sym
        }
        return null
    }
}