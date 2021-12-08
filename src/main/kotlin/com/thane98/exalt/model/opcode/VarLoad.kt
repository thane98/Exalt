package com.thane98.exalt.model.opcode

import com.thane98.exalt.compiler.CodegenUtils
import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState

class VarLoad(private val operand: Int) : Opcode {
    override fun generateV1(state: CodegenState) {
        CodegenUtils.generateVariableLength(state, operand, 0x1, 0x2)
    }

    override fun generateV3(state: CodegenState) {
        generateV1(state)
    }
}