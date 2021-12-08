package com.thane98.exalt.io

import java.nio.ByteBuffer
import java.nio.ByteOrder

class InMemoryBinaryReader(
    private val buffer: ByteArray,
    override var position: Int = 0
) : AbstractBinaryReader() {
    override fun readBytes(count: Int, order: ByteOrder): ByteBuffer {
        if (position + count > buffer.size) {
            throw IndexOutOfBoundsException("Out of bounds read position=$position, count=$count")
        }
        val buffer = ByteBuffer.allocate(4).order(order).put(buffer, position, count)
        position += count
        return buffer
    }

    override fun readByte(): Byte {
        val value = buffer[position]
        position++
        return value
    }
}