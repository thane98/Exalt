package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult

class MatchStartOpcodeDecompiler : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        val switch = state.expressions.peek()
        state.matches.start(switch)
        return DecompileStepResult.OTHER
    }
}