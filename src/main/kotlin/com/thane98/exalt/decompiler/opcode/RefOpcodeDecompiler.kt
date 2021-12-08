package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.decompiler.wrapAsAddrFuncall
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.IntType
import com.thane98.exalt.model.expr.Ref
import com.thane98.exalt.model.symbol.VarSymbol

abstract class RefOpcodeDecompiler(
    intType: IntType,
    private val addr: Boolean,
    private val global: Boolean
) : AbstractIntOpcodeDecompiler(intType) {
    override fun decompile(state: CodeDecompilerState, value: Int): DecompileStepResult {
        var symbol = state.symbolTable.lookupOrNull(VarSymbol.parameterName(value)) as? VarSymbol
        if (symbol == null) {
            val anonymousName = VarSymbol.anonymousName(value)
            symbol = state.symbolTable.lookupOrNull(anonymousName) as? VarSymbol
            if (symbol == null) {
                symbol = VarSymbol(anonymousName, global, value)
            }
        }

        val ref = decompile(state, symbol)
        state.expressions.push(if (addr) wrapAsAddrFuncall(state.symbolTable, ref) else ref)
        return DecompileStepResult.EXPR
    }

    protected abstract fun decompile(state: CodeDecompilerState, symbol: VarSymbol): Ref
}