package com.thane98.exalt.model.opcode

import com.thane98.exalt.compiler.CodegenUtils
import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState

class CallById(private val callId: Short) : Opcode {
    override fun generateV1(state: CodegenState) {
        state.output.add(0x37)
        if (callId <= 0x7F) {
            state.output.add(callId.toByte())
        } else {
            val realCallId = 0x8000.or(callId.toInt()).toShort()
            state.output.addAll(CodegenUtils.toBigEndianBytes(realCallId).toList())
        }
    }

    override fun generateV3(state: CodegenState) {
        state.output.add(0x46)
        if (callId <= 0x7F) {
            state.output.add(callId.toByte())
        } else {
            // TODO: This is NOT the correct logic for V3.
            //       Need to revisit this later.
            val realCallId = 0x8000.or(callId.toInt()).toShort()
            state.output.addAll(CodegenUtils.toBigEndianBytes(realCallId).toList())
        }
    }
}