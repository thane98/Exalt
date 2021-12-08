package com.thane98.exalt.model.symbol

data class LabelSymbol(override val name: String, var address: Int? = null) : Symbol
