package com.thane98.exalt.interfaces

import com.thane98.exalt.model.expr.Literal

interface ArgReader {
    fun readArgs(reader: BinaryReader, textDataVendor: TextDataVendor, type: Int, count: Int): List<Literal>
}