package com.thane98.exalt.model.opcode

class JumpNotZero(label: String) : AbstractJumpOpcode(label) {
    override fun v1Opcode(): Byte {
        return 0x3B
    }

    override fun v3Opcode(): Byte {
        return 0x4A
    }
}