package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.HeaderSerializer
import com.thane98.exalt.model.VersionInfo
import java.nio.charset.Charset

class V1HeaderSerializer(private val versionInfo: VersionInfo) : HeaderSerializer {
    companion object {
        const val MAX_NAME_LENGTH = 0x13
    }

    override fun serialize(scriptName: String, scriptType: Int): MutableList<Byte> {
        val nameBytes = scriptName.toByteArray(Charset.forName("Shift-JIS")).toList()
        if (nameBytes.size > MAX_NAME_LENGTH) {
            throw IllegalArgumentException("Script name '$scriptName' is too long for this version.")
        }

        val output = mutableListOf<Byte>()
        output.addAll(listOf(0x63, 0x6D, 0x62, 0)) // Magic number
        output.addAll(nameBytes)
        while (output.size < 0x18) {
            output.add(0)
        }
        output.addAll(CodegenUtils.toLittleEndianBytes(versionInfo.expectedVersion).toList())
        for (i in 0 until 4) {
            output.add(0)
        }
        output.addAll(CodegenUtils.toLittleEndianBytes(scriptType).toList())
        for (i in 0 until 8) {
            output.add(0)
        }
        return output
    }
}