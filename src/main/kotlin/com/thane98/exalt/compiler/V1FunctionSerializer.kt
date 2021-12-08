package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.FunctionSerializer
import com.thane98.exalt.model.CodegenFunctionData

class V1FunctionSerializer : FunctionSerializer {
    override fun serialize(functionData: CodegenFunctionData, id: Int, baseAddress: Int): List<Byte> {
        val header = functionData.header
        val output = mutableListOf<Byte>()
        val nameAddress = if (header.nameAddress == 0) 0 else header.nameAddress + baseAddress
        val parentAddress = if (header.argsAddress == 0) 0 else header.parentAddress + baseAddress
        output.addAll(CodegenUtils.toLittleEndianBytes(nameAddress).toList())
        output.addAll(CodegenUtils.toLittleEndianBytes(baseAddress + header.codeAddress).toList())
        output.addAll(CodegenUtils.toLittleEndianBytes(parentAddress).toList())
        output.add(header.type.toByte())
        output.add(header.arity.toByte())
        output.add(header.argCount.toByte())
        output.add(0)
        output.addAll(CodegenUtils.toLittleEndianBytes(id.toShort()).toList())
        output.addAll(CodegenUtils.toLittleEndianBytes(header.frameSize.toShort()).toList())
        output.addAll(functionData.args)
        output.addAll(functionData.name)
        while (output.size % 4 != 0) {
            output.add(0)
        }
        output.addAll(functionData.code)
        return output
    }
}