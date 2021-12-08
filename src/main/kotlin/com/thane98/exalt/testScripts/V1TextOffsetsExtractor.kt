package com.thane98.exalt.testScripts

import com.thane98.exalt.io.InMemoryBinaryReader
import com.thane98.exalt.model.Game
import com.thane98.exalt.model.HardCodedTextData
import com.thane98.exalt.model.VersionInfo
import java.nio.charset.Charset

class V1TextOffsetsExtractor(
    private val versionInfo: VersionInfo,
) {
    fun extract(rawScript: ByteArray): HardCodedTextData {
        val reader = InMemoryBinaryReader(rawScript, versionInfo.textDataPointerAddress)
        val textDataAddress = reader.readLittleEndianInt()
        reader.position = versionInfo.functionTablePointerAddress
        val functionTableAddress = reader.readLittleEndianInt()
        reader.position = textDataAddress

        val extractedText = mutableMapOf<String, Int>()
        while (reader.position < functionTableAddress) {
            val pos = reader.position - textDataAddress
            val buf = mutableListOf<Byte>()
            var nextByte = reader.readByte()
            while (nextByte.toInt() != 0) {
                buf.add(nextByte)
                nextByte = reader.readByte()
            }
            val str = String(buf.toByteArray(), Charset.forName("Shift-JIS"))
            if (str !in extractedText) {
                extractedText[str] = pos
            }
        }

        return HardCodedTextData(
            rawScript.slice(textDataAddress until functionTableAddress).toByteArray(),
            extractedText
        )
    }
}