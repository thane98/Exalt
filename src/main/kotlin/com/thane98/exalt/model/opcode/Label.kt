package com.thane98.exalt.model.opcode

import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState

class Label(private val label: String) : Opcode {
    override fun generateV1(state: CodegenState) {
        state.jumpTracker.resolveLabel(label, state.output.size)
    }

    override fun generateV3(state: CodegenState) {
        generateV1(state)
    }
}