package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.AstToOpcodeConverter
import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState
import com.thane98.exalt.model.CompilerFeatures
import com.thane98.exalt.model.opcode.IntLoad
import com.thane98.exalt.model.opcode.Return
import com.thane98.exalt.model.opcode.ReturnFalse

class V3CodeSerializer(
    astToOpcodeConverter: AstToOpcodeConverter = VisitorBasedAstToOpcodeConverter(),
    jumpTracker: JumpTracker = JumpTracker()
) : AbstractCodeSerializer(astToOpcodeConverter, jumpTracker) {
    override fun processOpcodes(state: CodegenState, features: CompilerFeatures, opcodes: List<Opcode>) {
        opcodes.forEach { it.generateV3(state) }
        if (features.useLongReturn) {
            IntLoad(0).generateV3(state)
            Return().generateV3(state)
        } else {
            ReturnFalse().generateV3(state)
        }
        state.output.add(0)
    }
}