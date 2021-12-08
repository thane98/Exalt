package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.IntType

abstract class AbstractIntOpcodeDecompiler(private val intType: IntType) : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        return decompile(state, when(intType) {
            IntType.BYTE -> state.reader.readByte().toInt()
            IntType.SHORT -> state.reader.readBigEndianShort()
            IntType.INT -> state.reader.readBigEndianInt()
        })
    }

    protected abstract fun decompile(state: CodeDecompilerState, value: Int): DecompileStepResult
}