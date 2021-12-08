package com.thane98.exalt.model.opcode

import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState

class StringNotEqual : Opcode {
    override fun generateV1(state: CodegenState) {
        state.output.add(0x36)
    }

    override fun generateV3(state: CodegenState) {
        throw UnsupportedOperationException("Operation is not supported by this script version.")
    }
}