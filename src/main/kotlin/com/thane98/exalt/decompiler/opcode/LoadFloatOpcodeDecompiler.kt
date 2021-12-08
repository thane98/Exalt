package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.expr.Literal

class LoadFloatOpcodeDecompiler : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        state.expressions.push(Literal.ofFloat(state.reader.readBigEndianFloat()))
        return DecompileStepResult.EXPR
    }
}