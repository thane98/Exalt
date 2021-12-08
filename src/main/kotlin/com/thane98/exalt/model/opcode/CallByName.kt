package com.thane98.exalt.model.opcode

import com.thane98.exalt.compiler.CodegenUtils
import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState

class CallByName(
    private val name: String,
    private val argCount: Byte,
) : Opcode {
    override fun generateV1(state: CodegenState) {
        val offset = state.textDataCreator.add(name).toShort()
        state.output.add(0x38)
        state.output.addAll(CodegenUtils.toBigEndianBytes(offset).toList())
        state.output.add(argCount)
    }

    override fun generateV3(state: CodegenState) {
        val offset = state.textDataCreator.add(name).toShort()
        state.output.add(0x47)
        state.output.addAll(CodegenUtils.toBigEndianBytes(offset).toList())
        state.output.add(argCount)
    }
}