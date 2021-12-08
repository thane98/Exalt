package com.thane98.exalt.interfaces

interface BinaryReader {
    var position: Int
    fun readLittleEndianShort(): Int
    fun readBigEndianShort(): Int
    fun readLittleEndianInt(): Int
    fun readBigEndianInt(): Int
    fun readLittleEndianFloat(): Float
    fun readBigEndianFloat(): Float
    fun readByte(): Byte
}