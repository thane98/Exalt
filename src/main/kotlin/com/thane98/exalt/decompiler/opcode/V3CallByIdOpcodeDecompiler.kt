package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.BinaryReader

class V3CallByIdOpcodeDecompiler : AbstractCallByIdOpcodeDecompiler() {
    override fun readId(reader: BinaryReader): Int {
        val b1 = reader.readByte().toInt()
        return if (b1.and(0b10000000) != 0) {
            b1.and(0x7F) * 0x80 + reader.readByte().toInt()
        } else {
            b1
        }
    }
}