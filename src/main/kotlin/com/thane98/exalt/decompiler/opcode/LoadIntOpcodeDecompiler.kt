package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.IntType
import com.thane98.exalt.model.expr.Literal

class LoadIntOpcodeDecompiler(intType: IntType) : AbstractIntOpcodeDecompiler(intType) {
    override fun decompile(state: CodeDecompilerState, value: Int): DecompileStepResult {
        state.expressions.push(Literal.ofInt(value))
        return DecompileStepResult.EXPR
    }
}