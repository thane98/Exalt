package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.IntType
import com.thane98.exalt.model.expr.ArrayRef
import com.thane98.exalt.model.expr.Ref
import com.thane98.exalt.model.symbol.VarSymbol

class ArrayRefOpcodeDecompiler(
    intType: IntType,
    addr: Boolean = false,
    global: Boolean = false,
) : RefOpcodeDecompiler(intType, addr, global) {
    override fun decompile(state: CodeDecompilerState, symbol: VarSymbol): Ref {
        return ArrayRef(symbol, state.expressions.pop())
    }
}