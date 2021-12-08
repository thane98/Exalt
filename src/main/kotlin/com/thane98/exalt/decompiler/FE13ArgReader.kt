package com.thane98.exalt.decompiler

import com.thane98.exalt.model.LiteralType

class FE13ArgReader : V3ArgReader() {
    companion object {
        private val SIGNATURES = mapOf(
            16 to listOf(
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.STR
            ),
            17 to listOf(
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.INT,
                LiteralType.STR
            ),
            18  to listOf(
                LiteralType.STR,
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.STR
            ),
            19  to listOf(
                LiteralType.STR,
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.STR
            ),
            21  to listOf(
                LiteralType.STR,
                LiteralType.INT,
                LiteralType.STR
            ),
            23  to listOf(
                LiteralType.STR,
                LiteralType.STR
            ),
        )
    }

    override fun signature(type: Int): List<LiteralType>? {
        return SIGNATURES[type]
    }
}