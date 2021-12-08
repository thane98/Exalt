package com.thane98.exalt.interfaces

import com.thane98.exalt.model.CodegenState

interface Opcode {
    fun generateV1(state: CodegenState)
    fun generateV2(state: CodegenState) { generateV1(state) }
    fun generateV3(state: CodegenState)
}