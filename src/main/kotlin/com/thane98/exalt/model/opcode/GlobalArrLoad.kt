package com.thane98.exalt.model.opcode

import com.thane98.exalt.compiler.CodegenUtils
import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState

class GlobalArrLoad(private val operand: Int) : Opcode {
    override fun generateV1(state: CodegenState) {
        CodegenUtils.generateVariableLength(state, operand, 0xF, 0x10)
    }

    override fun generateV3(state: CodegenState) {
        generateV1(state)
    }
}