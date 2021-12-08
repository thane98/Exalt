package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.AstToOpcodeConverter
import com.thane98.exalt.interfaces.CodeSerializer
import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.interfaces.TextDataCreator
import com.thane98.exalt.model.CodegenState
import com.thane98.exalt.model.CompilerFeatures
import com.thane98.exalt.model.stmt.Stmt

abstract class AbstractCodeSerializer(
    private val astToOpcodeConverter: AstToOpcodeConverter,
    private val jumpTracker: JumpTracker,
) : CodeSerializer {
    override fun serialize(textDataCreator: TextDataCreator, features: CompilerFeatures, code: Stmt): List<Byte> {
        jumpTracker.reset()
        val state = CodegenState(mutableListOf(), textDataCreator, jumpTracker)
        val opcodes = astToOpcodeConverter.convert(code, features)
        processOpcodes(state, features, opcodes)
        jumpTracker.resolveAllJumps(state.output)
        return state.output
    }

    abstract fun processOpcodes(state: CodegenState, features: CompilerFeatures, opcodes: List<Opcode>)
}