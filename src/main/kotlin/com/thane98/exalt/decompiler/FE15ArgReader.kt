package com.thane98.exalt.decompiler

import com.thane98.exalt.model.LiteralType

class FE15ArgReader : V3ArgReader() {
    companion object {
        private val SIGNATURES = mapOf(
            20 to listOf(
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.STR,
            ),
            21 to listOf(
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.STR,
                LiteralType.STR,
                LiteralType.STR,
            ),
            23 to listOf(
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
            ),
            26 to listOf(
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.STR,
                LiteralType.INT,
            ),
            27 to listOf(
                LiteralType.STR,
                LiteralType.INT,
            ),
            28 to listOf(
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.STR,
            ),
            37 to listOf(
                LiteralType.STR,
                LiteralType.INT,
            ),
            38 to listOf(
                LiteralType.STR,
                LiteralType.STR,
            )
        )
    }

    override fun signature(type: Int): List<LiteralType>? {
        return SIGNATURES[type]
    }
}