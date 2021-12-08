package com.thane98.exalt.io

import com.thane98.exalt.interfaces.BinaryReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class AbstractBinaryReader : BinaryReader {
    override fun readLittleEndianFloat(): Float {
        return readBytes(4, ByteOrder.LITTLE_ENDIAN).getFloat(0)
    }

    override fun readBigEndianFloat(): Float {
        return readBytes(4, ByteOrder.BIG_ENDIAN).getFloat(0)
    }

    override fun readLittleEndianShort(): Int {
        return readBytes(2, ByteOrder.LITTLE_ENDIAN).getShort(0).toInt()
    }

    override fun readBigEndianShort(): Int {
        return readBytes(2, ByteOrder.BIG_ENDIAN).getShort(0).toInt()
    }

    override fun readLittleEndianInt(): Int {
        return readBytes(4, ByteOrder.LITTLE_ENDIAN).getInt(0)
    }

    override fun readBigEndianInt(): Int {
        return readBytes(4, ByteOrder.BIG_ENDIAN).getInt(0)
    }

    protected abstract fun readBytes(count: Int, order: ByteOrder): ByteBuffer
}