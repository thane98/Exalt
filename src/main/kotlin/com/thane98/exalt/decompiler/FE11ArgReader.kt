package com.thane98.exalt.decompiler

import com.thane98.exalt.model.LiteralType

class FE11ArgReader : V1ArgReader() {
    companion object {
        private val SIGNATURES = mutableMapOf(
            0x1 to listOf(
                LiteralType.STR
            ),
            0x4 to listOf(
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.STR,
            ),
            0x5 to listOf(
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.STR,
            ),
            0x8 to listOf(
                LiteralType.STR,
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.STR,
            ),
            0x9 to listOf(
                LiteralType.STR,
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.STR,
            ),
            0xB to listOf(
                LiteralType.STR
            ),
            0xC to listOf(
                LiteralType.STR
            ),
            0xE to listOf(
                LiteralType.STR,
                LiteralType.STR,
            ),
            0x11 to listOf(
                LiteralType.STR,
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.STR,
            ),
            0x12 to listOf(
                LiteralType.STR,
                LiteralType.STR,
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.STR,
            ),
            0x13 to listOf(
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.STR
            ),
            0x14 to listOf(
                LiteralType.STR,
                LiteralType.STR
            )
        )
    }

    override fun signature(type: Int): List<LiteralType>? {
        return SIGNATURES[type]
    }
}