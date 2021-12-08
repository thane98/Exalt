package com.thane98.exalt.model.symbol

data class FunctionSymbol(
    override val name: String,
    val arity: Int,
    var callId: Int? = null
) : Symbol