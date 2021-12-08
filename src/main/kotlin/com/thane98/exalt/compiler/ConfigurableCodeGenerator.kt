package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.*
import com.thane98.exalt.model.Script
import com.thane98.exalt.model.VersionInfo

class ConfigurableCodeGenerator(
    private val versionInfo: VersionInfo,
    private val headerSerializer: HeaderSerializer,
    private val declToFunctionDataConverter: DeclToFunctionDataConverter,
    private val functionSerializer: FunctionSerializer,
    private val textDataCreator: TextDataCreator,
) : CodeGenerator {
    override fun generate(script: Script, scriptName: String): ByteArray {
        // Serialize functions.
        val functionData = declToFunctionDataConverter.convertAllDecls(script.contents)
        val rawTextData = textDataCreator.buildRawTextData()

        // Build the output.
        val output = headerSerializer.serialize(scriptName, script.type)

        // Handle version differences for text data placement (GCN/V1/V2)
        var textDataAddress = 0
        if (versionInfo.textBeforeFunctions) {
            textDataAddress = output.size
            output.addAll(rawTextData.toList())
            while (output.size % 4 != 0) {
                output.add(0)
            }
        }

        // Place functions.
        val functionBytes = mutableListOf<Byte>()
        val functionAddresses = mutableListOf<Int>()
        val functionTableLength = (functionData.size + 1) * 4 // Extra entry for null terminator.
        for (i in functionData.indices) {
            val function = functionData[i]
            val baseAddress = output.size + functionTableLength + functionBytes.size
            functionAddresses.add(baseAddress)
            functionBytes.addAll(functionSerializer.serialize(function, i, baseAddress))
            if (i != functionData.size - 1 || versionInfo.padLastFunction) {
                while (functionBytes.size % 4 != 0) {
                    functionBytes.add(0)
                }
            }
        }

        // Build the function table.
        val functionTableAddress = output.size
        for (address in functionAddresses) {
            output.addAll(CodegenUtils.toLittleEndianBytes(address).toList())
        }
        output.addAll(listOf(0, 0, 0, 0)) // Null terminator.
        output.addAll(functionBytes)

        // Handle version differences for text data placement (3DS/V3)
        if (!versionInfo.textBeforeFunctions) {
            textDataAddress = output.size
            output.addAll(rawTextData.toList())
        }

        // Fill in the header now that we know where text and functions got placed.
        val rawFunctionTableAddress = CodegenUtils.toLittleEndianBytes(functionTableAddress)
        val rawTextDataAddress = CodegenUtils.toLittleEndianBytes(textDataAddress)
        for (i in 0 until 4) {
            output[versionInfo.functionTablePointerAddress + i] = rawFunctionTableAddress[i]
            output[versionInfo.textDataPointerAddress + i] = rawTextDataAddress[i]
        }

        while (output.size % 4 != 0) {
            output.add(0)
        }

        return output.toByteArray()
    }
}