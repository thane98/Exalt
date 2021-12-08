package com.thane98.exalt.decompiler

import com.thane98.exalt.interfaces.BinaryReader
import com.thane98.exalt.interfaces.TextDataVendor

abstract class V1ArgReader : AbstractArgReader() {
    override fun readInt(reader: BinaryReader): Int {
        return reader.readLittleEndianShort()
    }

    override fun readFloat(reader: BinaryReader): Float {
        throw UnsupportedOperationException()
    }

    override fun readString(reader: BinaryReader, textDataVendor: TextDataVendor): String {
        return textDataVendor.textAt(reader.readLittleEndianShort())
    }
}