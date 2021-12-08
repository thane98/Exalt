package com.thane98.exalt.model.opcode

import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState

abstract class AbstractJumpOpcode(private val label: String): Opcode {
    override fun generateV1(state: CodegenState) {
        state.output.add(v1Opcode())
        state.jumpTracker.createMarker(label, state.output.size)
        state.output.addAll(listOf(0, 0))
    }

    override fun generateV3(state: CodegenState) {
        state.output.add(v3Opcode())
        state.jumpTracker.createMarker(label, state.output.size)
        state.output.addAll(listOf(0, 0))
    }

    abstract fun v1Opcode(): Byte

    abstract fun v3Opcode(): Byte
}