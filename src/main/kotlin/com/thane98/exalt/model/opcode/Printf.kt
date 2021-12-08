package com.thane98.exalt.model.opcode

import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState

class Printf(private val argCount: Byte) : Opcode {
    override fun generateV1(state: CodegenState) {
        state.output.add(0x41)
        state.output.add(argCount)
    }

    override fun generateV3(state: CodegenState) {
        state.output.add(0x50)
        state.output.add(argCount)
    }
}