package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.stmt.Goto

class GotoOpcodeDecompiler : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        val address = state.reader.position + state.reader.readBigEndianShort()
        val symbol = state.labelVendor.labelAt(address)
        state.blocks.line(Goto(symbol))
        return DecompileStepResult.STMT
    }
}