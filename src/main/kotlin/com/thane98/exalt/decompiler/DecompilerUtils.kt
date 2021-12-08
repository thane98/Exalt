package com.thane98.exalt.decompiler

import com.thane98.exalt.common.SymbolTable
import com.thane98.exalt.interfaces.BinaryReader
import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.expr.Funcall
import com.thane98.exalt.model.expr.Ref
import com.thane98.exalt.model.symbol.FunctionSymbol
import java.nio.charset.Charset
import kotlin.math.exp

fun readShiftJis(reader: BinaryReader): String {
    val buffer = mutableListOf<Byte>()
    var next = reader.readByte()
    while (next.toInt() != 0) {
        buffer.add(next)
        next = reader.readByte()
    }
    return String(buffer.toByteArray(), Charset.forName("Shift-JIS"))
}

fun zeroToNull(value: Int): Int? {
    return if (value == 0) null else value
}

fun validateAddress(address: Int, length: Int, name: String, endIsValid: Boolean = false) {
    if ((endIsValid && address > length) || (!endIsValid && address >= length)) {
        throw IndexOutOfBoundsException("$name address '0x${address.toString(16)}' is out of bounds.")
    }
}

fun wrapAsAddrFuncall(symbolTable: SymbolTable, ref: Ref): Expr {
    var symbol = symbolTable.lookupOrNull("addr") as? FunctionSymbol
    if (symbol == null) {
        symbol = FunctionSymbol("addr", 1)
        symbolTable.define(symbol)
    }
    return Funcall(symbol, listOf(ref))
}
