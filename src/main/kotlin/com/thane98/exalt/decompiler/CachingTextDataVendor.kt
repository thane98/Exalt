package com.thane98.exalt.decompiler

import com.thane98.exalt.interfaces.TextDataVendor
import java.nio.charset.Charset

class CachingTextDataVendor(private val textData: ByteArray, private val startIndex: Int) : TextDataVendor {
    private val cache = mutableMapOf<Int, String>()
    private val charset = Charset.forName("Shift-JIS")

    override fun textAt(offset: Int): String {
        return if (offset in cache) {
            cache[offset]!!
        } else {
            var end = startIndex + offset
            while (textData[end].toInt() != 0) {
                end++
            }
            val text = String(textData, offset, end - offset, charset)
            cache[offset] = text
            text
        }
    }
}