package com.thane98.exalt.interfaces

import com.thane98.exalt.model.CodegenFunctionData

interface FunctionSerializer {
    fun serialize(functionData: CodegenFunctionData, id: Int, baseAddress: Int): List<Byte>
}