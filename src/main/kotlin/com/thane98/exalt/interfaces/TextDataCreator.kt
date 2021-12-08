package com.thane98.exalt.interfaces

interface TextDataCreator {
    fun add(text: String): Int
    fun buildRawTextData(): ByteArray
}