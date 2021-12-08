package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.expr.Funcall
import com.thane98.exalt.model.symbol.FunctionSymbol

class BuiltInFunctionDecompiler(private val symbol: FunctionSymbol) : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        val args = state.expressions.popFunctionArguments(symbol.arity)
        state.expressions.push(Funcall(symbol, args))
        return DecompileStepResult.EXPR
    }
}