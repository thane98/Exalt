package com.thane98.exalt.decompiler

import com.thane98.exalt.interfaces.ArgReader
import com.thane98.exalt.interfaces.BinaryReader
import com.thane98.exalt.interfaces.FunctionHeaderReader
import com.thane98.exalt.interfaces.TextDataVendor
import com.thane98.exalt.model.RawFunctionData

class V3FunctionHeaderReader(private val argReader: ArgReader) : FunctionHeaderReader {
    override fun readHeader(reader: BinaryReader, textDataVendor: TextDataVendor): RawFunctionData {
        reader.position += 4 // Skip self pointer
        val codeAddress = reader.readLittleEndianInt()
        val type = reader.readByte().toInt()
        val arity = reader.readByte().toInt()
        val frameSize = reader.readLittleEndianShort()
        val id = reader.readLittleEndianInt()
        val nameAddress = zeroToNull(reader.readLittleEndianInt())
        val argsAddress = zeroToNull(reader.readLittleEndianInt())
        val name = if (nameAddress != null) {
            reader.position = nameAddress
            readShiftJis(reader)
        } else {
            null
        }
        val args = if (argsAddress != null) {
            reader.position = argsAddress
            argReader.readArgs(reader, textDataVendor, type, arity)
        } else {
            listOf()
        }
        return RawFunctionData(codeAddress, null, name, args, arity, type, frameSize, id)
    }

}