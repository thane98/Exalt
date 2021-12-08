package com.thane98.exalt.common

import com.thane98.exalt.error.SymbolRedefinitionException
import com.thane98.exalt.model.symbol.Symbol

class Environment {
    private val symbols: MutableMap<String, Symbol> = mutableMapOf()

    fun define(symbol: Symbol) {
        if (symbol.name in symbols) {
            throw SymbolRedefinitionException(symbol.name)
        } else {
            symbols[symbol.name] = symbol
        }
    }

    fun lookupOrNull(name: String): Symbol? {
        return symbols[name]
    }
}