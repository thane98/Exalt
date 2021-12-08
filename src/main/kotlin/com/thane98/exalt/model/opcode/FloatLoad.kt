package com.thane98.exalt.model.opcode

import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FloatLoad(private val operand: Float) : Opcode {
    override fun generateV1(state: CodegenState) {
        throw IllegalArgumentException("Operation is not supported for this game.")
    }

    override fun generateV3(state: CodegenState) {
        val raw = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putFloat(operand).array()
        state.output.add(0x1F)
        raw.forEach { state.output.add(it) }
    }
}