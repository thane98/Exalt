package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.AstToOpcodeConverter
import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState
import com.thane98.exalt.model.CompilerFeatures
import com.thane98.exalt.model.opcode.IntLoad
import com.thane98.exalt.model.opcode.Return

class V1CodeSerializer(
    astToOpcodeConverter: AstToOpcodeConverter = VisitorBasedAstToOpcodeConverter(),
    jumpTracker: JumpTracker = JumpTracker()
) : AbstractCodeSerializer(astToOpcodeConverter, jumpTracker) {
    override fun processOpcodes(state: CodegenState, features: CompilerFeatures, opcodes: List<Opcode>) {
        opcodes.forEach { it.generateV1(state) }
        IntLoad(0).generateV1(state)
        Return().generateV1(state)
        state.output.add(0)
    }
}