package com.thane98.exalt.model.opcode

import com.thane98.exalt.compiler.CodegenUtils
import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState

class StringLoad(val operand: String) : Opcode {
    override fun generateV1(state: CodegenState) {
        val offset = state.textDataCreator.add(operand)
        CodegenUtils.generateVariableLength(state, offset, 0x1C, 0x1D, 0x1E)
    }

    override fun generateV3(state: CodegenState) {
        generateV1(state)
    }
}