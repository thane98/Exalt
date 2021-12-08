package com.thane98.exalt.model.opcode

import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState

class ReturnTrue : Opcode {
    override fun generateV1(state: CodegenState) {
        throw IllegalArgumentException("Operation is not supported by this script version.")
    }

    override fun generateV2(state: CodegenState) {
        state.output.add(0x46)
    }

    override fun generateV3(state: CodegenState) {
        state.output.add(0x55)
    }
}