package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.ArgSerializer
import com.thane98.exalt.interfaces.CodeSerializer
import com.thane98.exalt.interfaces.TextDataCreator
import com.thane98.exalt.model.CodegenFunctionData
import com.thane98.exalt.model.CodegenFunctionHeader
import com.thane98.exalt.model.decl.Decl
import com.thane98.exalt.model.decl.EventDecl
import com.thane98.exalt.model.decl.FunctionDecl

class V1DeclToFunctionDataConverter(
    textDataCreator: TextDataCreator,
    argSerializer: ArgSerializer,
    codeSerializer: CodeSerializer = V1CodeSerializer(),
) : AbstractDeclToFunctionDataConverter(textDataCreator, argSerializer, codeSerializer) {
    companion object {
        const val HEADER_SIZE = 0x14
    }

    override fun processDecl(
        textDataCreator: TextDataCreator,
        type: Int,
        frameSize: Int,
        arity: Int,
        argCount: Int,
        code: List<Byte>,
        args: List<Byte>,
        name: List<Byte>
    ): CodegenFunctionData {
        val nameAddress = if (name.isNotEmpty()) HEADER_SIZE + args.size else 0
        val unpaddedCodeAddress = HEADER_SIZE + args.size + name.size
        val paddedCodedAddress = unpaddedCodeAddress + (4 - unpaddedCodeAddress % 4) % 4
        val header = CodegenFunctionHeader(
            paddedCodedAddress,
            nameAddress,
            0,
            0,
            if (type != 0) 0 else arity,
            frameSize,
            type,
            argCount
        )
        return CodegenFunctionData(header, name, args, code)
    }

    override fun isAnonymousFunction(name: String): Boolean {
        return name.startsWith("anonfn")
    }
}