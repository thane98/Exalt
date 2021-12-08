package com.thane98.exalt.model.decl

import com.thane98.exalt.interfaces.DeclVisitor
import com.thane98.exalt.model.stmt.Block
import com.thane98.exalt.model.symbol.FunctionSymbol
import com.thane98.exalt.model.symbol.VarSymbol

data class FunctionDecl(
    val symbol: FunctionSymbol,
    val parameters: List<VarSymbol>,
    val body: Block,
    val annotations: MutableList<Annotation>,
    var frameSize: Int = -1,
): Decl {
    override fun <T> accept(visitor: DeclVisitor<T>): T {
        return visitor.visitFunctionDecl(this)
    }
}