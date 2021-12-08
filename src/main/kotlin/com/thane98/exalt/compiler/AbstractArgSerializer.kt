package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.ArgSerializer
import com.thane98.exalt.interfaces.TextDataCreator
import com.thane98.exalt.model.LiteralType
import com.thane98.exalt.model.expr.Literal

abstract class AbstractArgSerializer : ArgSerializer {
    override fun serialize(textDataCreator: TextDataCreator, args: List<Literal>): List<Byte> {
        val output = mutableListOf<Byte>()
        for (arg in args) {
            when (arg.literalType()) {
                LiteralType.INT -> serializeInt(output, arg.intValue())
                LiteralType.FLOAT -> serializeFloat(output, arg.floatValue())
                LiteralType.STR -> serializeText(output, textDataCreator, arg.stringValue())
            }
        }
        return output
    }

    protected abstract fun serializeInt(output: MutableList<Byte>, value: Int)

    protected abstract fun serializeFloat(output: MutableList<Byte>, value: Float)

    protected abstract fun serializeText(output: MutableList<Byte>, textDataCreator: TextDataCreator, value: String)
}