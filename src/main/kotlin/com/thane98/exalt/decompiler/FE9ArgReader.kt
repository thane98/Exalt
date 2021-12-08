package com.thane98.exalt.decompiler

import com.thane98.exalt.model.LiteralType

class FE9ArgReader : V1ArgReader() {
    companion object {
        private val SIGNATURES = mutableMapOf<Int, List<LiteralType>>()
    }

    override fun signature(type: Int): List<LiteralType>? {
        return SIGNATURES[type]
    }
}