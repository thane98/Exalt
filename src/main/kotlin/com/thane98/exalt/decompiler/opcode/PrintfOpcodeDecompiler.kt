package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.expr.Funcall
import com.thane98.exalt.model.stmt.ExprStmt
import com.thane98.exalt.model.symbol.FunctionSymbol

class PrintfOpcodeDecompiler : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        val arity = state.reader.readByte().toInt()
        val args = state.expressions.popFunctionArguments(arity)
        val symbol = FunctionSymbol("printf", arity)
        state.blocks.line(ExprStmt(Funcall(symbol, args)))
        return DecompileStepResult.STMT
    }
}