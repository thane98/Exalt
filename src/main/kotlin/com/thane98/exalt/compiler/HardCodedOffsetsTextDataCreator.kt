package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.TextDataCreator
import com.thane98.exalt.model.HardCodedTextData

/**
 * This is a utility for building text data with hard coded text placement + offsets.
 *
 * It is used for testing v1/v2 scripts because their text data doesn't seem to follow
 * a predictable pattern for where text ends up. Forcing hard coded offsets in the compiler
 * lets us test for exact offsets.
 */
class HardCodedOffsetsTextDataCreator(
    private val hardCodedTextData: HardCodedTextData
) : TextDataCreator {
    override fun add(text: String): Int {
        return hardCodedTextData.offsetMappings[text]!!
    }

    override fun buildRawTextData(): ByteArray {
        return hardCodedTextData.rawTextData
    }
}