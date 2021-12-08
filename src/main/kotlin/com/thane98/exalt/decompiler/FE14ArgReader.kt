package com.thane98.exalt.decompiler

import com.thane98.exalt.model.LiteralType

class FE14ArgReader : V3ArgReader() {
    companion object {
        private val SIGNATURES = mutableMapOf(
            0x14 to listOf(
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.STR,
            ),
            0x15 to listOf(
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.STR,
            ),
            0x17 to listOf(
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.STR,
            ),
            0x18 to listOf(
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.STR,
            ),
            0x19 to listOf(
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.STR,
            ),
            0x1B to listOf(
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.STR,
                LiteralType.INT,
            ),
            0x1C to listOf(
                LiteralType.STR,
                LiteralType.INT,
            ),
            0x1D to listOf(
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.STR,
            ),
            0x1E to listOf(
                LiteralType.STR,
            ),
            0x1F to listOf(
                LiteralType.STR,
            ),
            0x20 to listOf(
                LiteralType.STR,
                LiteralType.INT,
            ),
        )
    }

    override fun signature(type: Int): List<LiteralType>? {
        return SIGNATURES[type]
    }
}