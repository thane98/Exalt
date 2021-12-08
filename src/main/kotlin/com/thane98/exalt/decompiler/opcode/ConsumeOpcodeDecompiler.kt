package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.stmt.ExprStmt

class ConsumeOpcodeDecompiler : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        val expr = state.expressions.pop()
        state.blocks.line(ExprStmt(expr))
        return DecompileStepResult.STMT
    }
}