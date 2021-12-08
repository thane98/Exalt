package com.thane98.exalt.model.opcode

import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState

class BinaryOr : Opcode {
    override fun generateV1(state: CodegenState) {
        state.output.add(0x2A)
    }

    override fun generateV3(state: CodegenState) {
        state.output.add(0x33)
    }
}