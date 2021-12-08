package com.thane98.exalt.interfaces

import com.thane98.exalt.model.expr.Literal

interface ArgSerializer {
    fun serialize(textDataCreator: TextDataCreator, args: List<Literal>): List<Byte>
}