package com.thane98.exalt.decompiler

import com.thane98.exalt.interfaces.ArgReader
import com.thane98.exalt.interfaces.BinaryReader
import com.thane98.exalt.interfaces.FunctionHeaderReader
import com.thane98.exalt.interfaces.TextDataVendor
import com.thane98.exalt.model.RawFunctionData

class V1FunctionHeaderReader(private val argReader: ArgReader) : FunctionHeaderReader {
    override fun readHeader(reader: BinaryReader, textDataVendor: TextDataVendor): RawFunctionData {
        val nameAddress = zeroToNull(reader.readLittleEndianInt())
        val codeAddress = reader.readLittleEndianInt()
        val parentAddress = zeroToNull(reader.readLittleEndianInt())
        val type = reader.readByte().toInt()
        val arity = reader.readByte().toInt()
        val argCount = reader.readByte().toInt()
        reader.position += 1 // Skip padding
        val id = reader.readLittleEndianShort()
        val frameSize = reader.readLittleEndianShort()
        val name = if (nameAddress != null) {
            reader.position = nameAddress
            readShiftJis(reader)
        } else {
            null
        }
        val args = argReader.readArgs(reader, textDataVendor, type, argCount)
        return RawFunctionData(codeAddress, parentAddress, name, args, arity, type, frameSize, id)
    }
}