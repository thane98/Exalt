package com.thane98.exalt.model.opcode

class JumpZero(label: String) : AbstractJumpOpcode(label) {
    override fun v1Opcode(): Byte {
        return 0x3D
    }

    override fun v3Opcode(): Byte {
        return 0x4C
    }
}