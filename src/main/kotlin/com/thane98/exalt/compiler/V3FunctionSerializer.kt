package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.FunctionSerializer
import com.thane98.exalt.model.CodegenFunctionData

class V3FunctionSerializer : FunctionSerializer {
    override fun serialize(functionData: CodegenFunctionData, id: Int, baseAddress: Int): List<Byte> {
        val header = functionData.header
        val output = mutableListOf<Byte>()
        val nameAddress = if (header.nameAddress == 0) 0 else header.nameAddress + baseAddress
        val argsAddress = if (header.argsAddress == 0) 0 else header.argsAddress + baseAddress
        output.addAll(CodegenUtils.toLittleEndianBytes(baseAddress).toList())
        output.addAll(CodegenUtils.toLittleEndianBytes(header.codeAddress + baseAddress).toList())
        output.add(header.type.toByte())
        output.add(header.arity.toByte())
        output.addAll(CodegenUtils.toLittleEndianBytes(header.frameSize.toShort()).toList())
        output.addAll(CodegenUtils.toLittleEndianBytes(id).toList())
        output.addAll(CodegenUtils.toLittleEndianBytes(nameAddress).toList())
        output.addAll(CodegenUtils.toLittleEndianBytes(argsAddress).toList())
        output.addAll(functionData.args)
        output.addAll(functionData.name)
        output.addAll(functionData.code)
        return output
    }
}