package com.thane98.exalt.decompiler

import com.thane98.exalt.common.SymbolTable
import com.thane98.exalt.interfaces.BinaryReader
import com.thane98.exalt.interfaces.CodeDecompiler
import com.thane98.exalt.interfaces.CodeDecompilerFactory
import com.thane98.exalt.interfaces.TextDataVendor
import com.thane98.exalt.model.symbol.FunctionSymbol

class V3CodeDecompilerFactory : CodeDecompilerFactory {
    override fun create(
        reader: BinaryReader,
        textDataVendor: TextDataVendor,
        functionIdToSymbol: Map<Int, FunctionSymbol>,
        symbolTable: SymbolTable
    ): CodeDecompiler {
        return V3CodeDecompiler(reader, textDataVendor, functionIdToSymbol, symbolTable)
    }
}