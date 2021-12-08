package com.thane98.exalt.model.expr

import com.thane98.exalt.model.symbol.VarSymbol

interface Ref : Expr {
    val symbol: VarSymbol
    var isPointer: Boolean
}