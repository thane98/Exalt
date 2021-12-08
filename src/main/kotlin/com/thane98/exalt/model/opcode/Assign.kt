package com.thane98.exalt.model.opcode

import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState

class Assign : Opcode {
    override fun generateV1(state: CodegenState) {
        state.output.add(0x21)
    }

    override fun generateV2(state: CodegenState) {
        state.output.add(0x47)
    }

    override fun generateV3(state: CodegenState) {
        state.output.add(0x23)
    }
}