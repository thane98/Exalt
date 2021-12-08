package com.thane98.exalt.model.opcode

class Jump(label: String) : AbstractJumpOpcode(label) {
    override fun v1Opcode(): Byte {
        return 0x3A
    }

    override fun v3Opcode(): Byte {
        return 0x49
    }
}