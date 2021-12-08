package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.IntType
import com.thane98.exalt.model.expr.Ref
import com.thane98.exalt.model.expr.VarRef
import com.thane98.exalt.model.symbol.VarSymbol

class VarRefOpcodeDecompiler(
    intType: IntType,
    addr: Boolean = false,
    global: Boolean = false,
) : RefOpcodeDecompiler(intType, addr, global) {
    override fun decompile(state: CodeDecompilerState, symbol: VarSymbol): Ref {
        return VarRef(symbol)
    }
}