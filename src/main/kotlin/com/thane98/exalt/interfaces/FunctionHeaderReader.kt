package com.thane98.exalt.interfaces

import com.thane98.exalt.model.RawFunctionData

interface FunctionHeaderReader {
    fun readHeader(reader: BinaryReader, textDataVendor: TextDataVendor): RawFunctionData
}