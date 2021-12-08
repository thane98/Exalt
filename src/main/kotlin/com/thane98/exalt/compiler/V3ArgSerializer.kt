package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.TextDataCreator

class V3ArgSerializer : AbstractArgSerializer() {
    override fun serializeInt(output: MutableList<Byte>, value: Int) {
        output.addAll(CodegenUtils.toLittleEndianBytes(value).toList())
    }

    override fun serializeFloat(output: MutableList<Byte>, value: Float) {
        output.addAll(CodegenUtils.toLittleEndianBytes(value).toList())
    }

    override fun serializeText(output: MutableList<Byte>, textDataCreator: TextDataCreator, value: String) {
        output.addAll(CodegenUtils.toLittleEndianBytes(textDataCreator.add(value)).toList())
    }
}