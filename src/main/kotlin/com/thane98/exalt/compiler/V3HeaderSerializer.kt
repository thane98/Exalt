package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.HeaderSerializer
import java.nio.charset.Charset

class V3HeaderSerializer : HeaderSerializer {
    override fun serialize(scriptName: String, scriptType: Int): MutableList<Byte> {
        val nameBytes = scriptName.toByteArray(Charset.forName("Shift-JIS")).toList()
        val output = mutableListOf<Byte>()
        output.addAll(listOf(0x63, 0x6D, 0x62, 0)) // Magic number
        output.addAll(listOf(0x19, 0x08, 0x11, 0x20)) // Revision date
        output.addAll(listOf(0, 0, 0, 0, 0x28, 0, 0, 0)) // Padding and name pointer.
        while (output.size < 0x24) {
            output.add(0) // Padding and event / text pointers. Will resolve later when we know where they are.
        }
        output.addAll(CodegenUtils.toLittleEndianBytes(scriptType).toList())
        output.addAll(nameBytes)
        output.add(0)
        while (output.size % 4 != 0) {
            output.add(0)
        }
        return output
    }
}