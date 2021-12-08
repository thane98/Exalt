package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.error.NegativeBranchException
import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.stmt.Goto
import com.thane98.exalt.model.stmt.If
import com.thane98.exalt.model.stmt.Stmt

class IfOpcodeDecompiler : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        val offset = state.reader.readBigEndianShort() - 2
        if (offset < 0) {
            throw NegativeBranchException()
        }

        val condition = state.expressions.pop()
        state.blocks.push()
        state.codeDecompiler.decompileBlock(state, offset)
        val thenPart = state.blocks.pop()

        state.blocks.line(If(condition, thenPart, null))
        return DecompileStepResult.STMT
    }
}