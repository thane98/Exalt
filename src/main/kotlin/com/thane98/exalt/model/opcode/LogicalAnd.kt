package com.thane98.exalt.model.opcode

class LogicalAnd(label: String) : AbstractJumpOpcode(label) {
    override fun v1Opcode(): Byte {
        return 0x3E
    }

    override fun v3Opcode(): Byte {
        return 0x4D
    }
}