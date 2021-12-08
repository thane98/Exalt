package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.TextDataCreator
import java.nio.charset.Charset

class CachingTextDataCreator : TextDataCreator {
    private val cache = mutableMapOf<String, Int>()
    private val rawTextData = mutableListOf<Byte>()

    override fun add(text: String): Int {
        return if (text in cache) {
            cache[text]!!
        } else {
            val offset = rawTextData.size
            val raw = text.toByteArray(Charset.forName("Shift-JIS"))
            raw.forEach { rawTextData.add(it) }
            rawTextData.add(0)
            cache[text] = offset
            offset
        }
    }

    override fun buildRawTextData(): ByteArray {
        return rawTextData.toByteArray()
    }
}