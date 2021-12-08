package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.AstToOpcodeConverter
import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.CodegenState
import com.thane98.exalt.model.CompilerFeatures
import com.thane98.exalt.model.opcode.IntLoad
import com.thane98.exalt.model.opcode.Return
import com.thane98.exalt.model.opcode.ReturnFalse
import com.thane98.exalt.model.opcode.ReturnTrue

class V2CodeSerializer(
    astToOpcodeConverter: AstToOpcodeConverter = VisitorBasedAstToOpcodeConverter(),
    jumpTracker: JumpTracker = JumpTracker()
) : AbstractCodeSerializer(astToOpcodeConverter, jumpTracker) {
    override fun processOpcodes(state: CodegenState, features: CompilerFeatures, opcodes: List<Opcode>) {
        opcodes.forEach { it.generateV2(state) }
        if (features.useLongReturn) {
            IntLoad(0).generateV2(state)
            Return().generateV2(state)
        } else {
            ReturnFalse().generateV2(state)
        }
        state.output.add(0)
    }
}