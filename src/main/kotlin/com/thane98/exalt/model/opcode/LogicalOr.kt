package com.thane98.exalt.model.opcode

class LogicalOr(label: String) : AbstractJumpOpcode(label) {
    override fun v1Opcode(): Byte {
        return 0x3C
    }

    override fun v3Opcode(): Byte {
        return 0x4B
    }
}