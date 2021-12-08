package com.thane98.exalt.decompiler

import com.thane98.exalt.interfaces.ArgReader
import com.thane98.exalt.interfaces.BinaryReader
import com.thane98.exalt.interfaces.TextDataVendor
import com.thane98.exalt.model.LiteralType
import com.thane98.exalt.model.expr.Literal

abstract class AbstractArgReader : ArgReader {
    override fun readArgs(reader: BinaryReader, textDataVendor: TextDataVendor, type: Int, count: Int): List<Literal> {
        val args = mutableListOf<Literal>()
        val sig = signature(type)
        if (sig == null) {
            for (i in 0 until count) {
                args.add(Literal.ofInt(readInt(reader)))
            }
        } else {
            if (sig.size != count) {
                throw IllegalArgumentException(
                    "Signature for type '$type' has length '${sig.size}' but actual length is $count"
                )
            }
            for (i in 0 until count) {
                args.add(
                    when (sig[i]) {
                        LiteralType.INT -> Literal.ofInt(readInt(reader))
                        LiteralType.FLOAT -> Literal.ofFloat(readFloat(reader))
                        LiteralType.STR -> Literal.ofString(readString(reader, textDataVendor))
                    }
                )
            }
        }
        return args
    }

    abstract fun readInt(reader: BinaryReader): Int

    abstract fun readFloat(reader: BinaryReader): Float

    abstract fun readString(reader: BinaryReader, textDataVendor: TextDataVendor): String

    abstract fun signature(type: Int): List<LiteralType>?
}