package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.BinaryReader
import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.expr.Funcall

abstract class AbstractCallByIdOpcodeDecompiler : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        val id = readId(state.reader)
        val symbol = state.functionIdToSymbol[id]
            ?: throw IllegalArgumentException("ID ${id.toString(16)} does not map to a valid function.")
        val args = state.expressions.popFunctionArguments(symbol.arity)
        state.expressions.push(Funcall(symbol, args))
        return DecompileStepResult.EXPR
    }

    protected abstract fun readId(reader: BinaryReader): Int
}