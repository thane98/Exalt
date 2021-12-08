package com.thane98.exalt.common

import com.thane98.exalt.error.BadScopeException
import com.thane98.exalt.error.SymbolNotFoundException
import com.thane98.exalt.model.symbol.Symbol

class SymbolTable {
    private val environments = ArrayDeque<Environment>()

    init {
        environments.addLast(Environment())
    }

    fun openNewEnvironment() {
        environments.addLast(Environment())
    }

    fun closeEnvironment() {
        environments.removeLast()
    }

    fun define(symbol: Symbol) {
        environments.last().define(symbol)
    }

    fun defineTopLevel(symbol: Symbol) {
        if (environments.isEmpty()) {
            throw BadScopeException()
        } else {
            environments[0].define(symbol)
        }
    }

    fun defineInFunctionScope(symbol: Symbol) {
        if (environments.size < 2) {
            throw BadScopeException()
        } else {
            environments[1].define(symbol)
        }
    }

    fun lookupOrNull(name: String): Symbol? {
        for (env in environments.reversed()) {
            val symbol = env.lookupOrNull(name)
            if (symbol != null) {
                return symbol
            }
        }
        return null
    }

    fun lookup(name: String): Symbol {
        val symbol = lookupOrNull(name)
        if (symbol != null) {
            return symbol
        } else {
            throw SymbolNotFoundException(name)
        }
    }
}