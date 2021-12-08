package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.TextDataCreator
import java.lang.UnsupportedOperationException

class V1ArgSerializer : AbstractArgSerializer() {
    override fun serializeInt(output: MutableList<Byte>, value: Int) {
        output.addAll(CodegenUtils.toLittleEndianBytes(value.toShort()).toList())
    }

    override fun serializeFloat(output: MutableList<Byte>, value: Float) {
        throw UnsupportedOperationException("V1/V2 scripts do not support floats.")
    }

    override fun serializeText(output: MutableList<Byte>, textDataCreator: TextDataCreator, value: String) {
        output.addAll(CodegenUtils.toLittleEndianBytes(textDataCreator.add(value).toShort()).toList())
    }
}