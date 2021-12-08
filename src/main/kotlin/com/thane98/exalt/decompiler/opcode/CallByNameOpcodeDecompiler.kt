package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.expr.Funcall
import com.thane98.exalt.model.symbol.FunctionSymbol

class CallByNameOpcodeDecompiler : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        val name = state.textDataVendor.textAt(state.reader.readBigEndianShort())
        val arity = state.reader.readByte().toInt()
        val args = state.expressions.popFunctionArguments(arity)
        val symbol = FunctionSymbol(name, arity)
        state.expressions.push(Funcall(symbol, args))
        return DecompileStepResult.EXPR
    }
}