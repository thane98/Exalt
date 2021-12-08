package com.thane98.exalt.decompiler

import com.thane98.exalt.common.AnnotationAppender
import com.thane98.exalt.common.SymbolTable
import com.thane98.exalt.compiler.FrameDataCalculator
import com.thane98.exalt.interfaces.BinaryReader
import com.thane98.exalt.interfaces.DecompilerInfo
import com.thane98.exalt.interfaces.TextDataVendor
import com.thane98.exalt.io.InMemoryBinaryReader
import com.thane98.exalt.model.Game
import com.thane98.exalt.model.RawFunctionData
import com.thane98.exalt.model.Script
import com.thane98.exalt.model.decl.Annotation
import com.thane98.exalt.model.decl.Decl
import com.thane98.exalt.model.decl.EventDecl
import com.thane98.exalt.model.decl.FunctionDecl
import com.thane98.exalt.model.decl.ScriptDecl
import com.thane98.exalt.model.symbol.FunctionSymbol
import com.thane98.exalt.model.symbol.VarSymbol

class ScriptDecompiler(
    private val decompilerInfo: DecompilerInfo,
    private val game: Game
) {
    companion object {
        fun forGame(game: Game): ScriptDecompiler {
            return when (game) {
                Game.FE10 -> ScriptDecompiler(FE10DecompilerInfo(), game)
                Game.FE11 -> ScriptDecompiler(FE11DecompilerInfo(), game)
                Game.FE12 -> ScriptDecompiler(FE12DecompilerInfo(), game)
                Game.FE13 -> ScriptDecompiler(FE13DecompilerInfo(), game)
                Game.FE14 -> ScriptDecompiler(FE14DecompilerInfo(), game)
                Game.FE15 -> ScriptDecompiler(FE15DecompilerInfo(), game)
            }
        }
    }

    fun decompile(raw: ByteArray): Script {
        // Sanity checks.
        val versionInfo = decompilerInfo.versionInfo
        validateAddress(versionInfo.scriptTypeAddress, raw.size, "Script type")
        validateAddress(versionInfo.versionAddress, raw.size, "Version")
        validateAddress(versionInfo.functionTablePointerAddress, raw.size, "Function table pointer")
        validateAddress(versionInfo.textDataPointerAddress, raw.size, "Text data pointer")

        // Read the header.
        val reader = InMemoryBinaryReader(raw)
        reader.position = versionInfo.versionAddress
        if (reader.readLittleEndianInt() != versionInfo.expectedVersion) {
            throw IllegalArgumentException("Incorrect script version for this decompiler.")
        }
        reader.position = versionInfo.scriptTypeAddress
        val scriptType = reader.readLittleEndianInt()
        reader.position = versionInfo.textDataPointerAddress
        val textDataAddress = reader.readLittleEndianInt()
        reader.position = versionInfo.functionTablePointerAddress
        val functionTableAddress = reader.readLittleEndianInt()
        validateAddress(textDataAddress, raw.size, "Text data", endIsValid = true)
        validateAddress(functionTableAddress, raw.size, "Function table")

        // Read functions and text data.
        val textDataVendor = CachingTextDataVendor(raw.copyOfRange(textDataAddress, raw.size), 0)
        val functionPointers = readFunctionPointers(raw, functionTableAddress, reader)
        val functionHeaders = functionPointers.map { readFunctionHeader(raw, it, reader, textDataVendor) }
        val (symbolTable, functionIdToSymbol) = buildFunctionSymbolTableAndMapping(functionHeaders)

        // Decompile functions.
        val decls = mutableListOf<Decl>()
        decls.add(ScriptDecl(game, scriptType))
        for (header in functionHeaders) {
            decls.add(decompileFunction(header, reader, symbolTable, functionIdToSymbol, textDataVendor))
        }
        return Script(decls, game, scriptType)
    }

    private fun readFunctionPointers(raw: ByteArray, address: Int, reader: BinaryReader): List<Int> {
        reader.position = address
        val pointers = mutableListOf<Int>()
        var next = reader.readLittleEndianInt()
        while (next != 0) {
            validateAddress(next, raw.size, "Function ${pointers.size}")
            pointers.add(next)
            next = reader.readLittleEndianInt()
        }
        return pointers
    }

    private fun readFunctionHeader(
        raw: ByteArray,
        address: Int,
        reader: BinaryReader,
        textDataVendor: TextDataVendor
    ): RawFunctionData {
        reader.position = address
        val header = decompilerInfo.functionReader.readHeader(reader, textDataVendor)
        validateAddress(header.codeAddress, raw.size, "Function at '0x${address.toString(16)}' code")
        return header
    }

    private fun buildFunctionSymbolTableAndMapping(
        functionHeaders: List<RawFunctionData>
    ): Pair<SymbolTable, Map<Int, FunctionSymbol>> {
        val symbolTable = SymbolTable()
        val functionIdToSymbol = mutableMapOf<Int, FunctionSymbol>()
        for (header in functionHeaders) {
            if (header.type == 0) {
                val name = header.name ?: "anonfn${header.id}"
                val symbol = FunctionSymbol(name, header.arity, header.id)
                symbolTable.define(symbol)
                functionIdToSymbol[header.id] = symbol
            }
        }
        return Pair(symbolTable, functionIdToSymbol)
    }

    private fun decompileFunction(
        header: RawFunctionData,
        reader: BinaryReader,
        symbolTable: SymbolTable,
        functionIdToSymbol: Map<Int, FunctionSymbol>,
        textDataVendor: TextDataVendor,
    ): Decl {
        try {
            // Open environment for this function's variables and labels.
            symbolTable.openNewEnvironment()

            // Register parameters in the symbol table (not relevant for events).
            val parameters = if (header.type == 0) {
                val parameters = mutableListOf<VarSymbol>()
                for (i in 0 until header.arity) {
                    val paramName = "v$i"
                    val symbol = VarSymbol(paramName, false, i)
                    symbolTable.define(symbol)
                    parameters.add(symbol)
                }
                parameters
            } else {
                listOf()
            }

            // Decompile code.
            val factory = decompilerInfo.codeDecompilerFactory
            val decompiler = factory.create(reader, textDataVendor, functionIdToSymbol, symbolTable)
            val (body, annotations) = decompiler.decompile(header.codeAddress)

            // Build the declaration.
            val decl = if (header.type == 0) {
                FunctionDecl(functionIdToSymbol[header.id]!!, parameters, body, annotations.toMutableList())
            } else {
                EventDecl(header.type, header.args, body, annotations.toMutableList())
            }

            // Hack: Some functions reserve more space than Exalt thinks they need.
            //       To avoid unforeseen bugs in-game, we detect that here and append an annotation
            //       if the actual frame size is greater than the size Exalt calculates.
            val calculatedFrameSize = FrameDataCalculator.calculate(decl)
            if (calculatedFrameSize < header.frameSize) {
                val diff = header.frameSize - calculatedFrameSize
                AnnotationAppender.append(decl, Annotation("PadFrame", listOf(diff.toString())))
            }

            return decl
        } finally {
            symbolTable.closeEnvironment()
        }
    }
}