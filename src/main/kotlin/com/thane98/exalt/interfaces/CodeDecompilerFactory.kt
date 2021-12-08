package com.thane98.exalt.interfaces

import com.thane98.exalt.common.SymbolTable
import com.thane98.exalt.model.symbol.FunctionSymbol

interface CodeDecompilerFactory {
    fun create(
        reader: BinaryReader,
        textDataVendor: TextDataVendor,
        functionIdToSymbol: Map<Int, FunctionSymbol>,
        symbolTable: SymbolTable
    ): CodeDecompiler
}