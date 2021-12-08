package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.IntType
import com.thane98.exalt.model.expr.Literal

class LoadStringOpcodeDecompiler(intType: IntType) : AbstractIntOpcodeDecompiler(intType) {
    override fun decompile(state: CodeDecompilerState, value: Int): DecompileStepResult {
        state.expressions.push(Literal.ofString(state.textDataVendor.textAt(value)))
        return DecompileStepResult.EXPR
    }
}