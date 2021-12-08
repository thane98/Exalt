package com.thane98.exalt.decompiler

import com.thane98.exalt.interfaces.BinaryReader
import com.thane98.exalt.interfaces.TextDataVendor

abstract class V3ArgReader : AbstractArgReader() {
    override fun readInt(reader: BinaryReader): Int {
        return reader.readLittleEndianInt()
    }

    override fun readFloat(reader: BinaryReader): Float {
        return reader.readLittleEndianFloat()
    }

    override fun readString(reader: BinaryReader, textDataVendor: TextDataVendor): String {
        return textDataVendor.textAt(reader.readLittleEndianInt())
    }
}