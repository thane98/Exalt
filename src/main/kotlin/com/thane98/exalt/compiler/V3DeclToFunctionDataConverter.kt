package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.ArgSerializer
import com.thane98.exalt.interfaces.CodeSerializer
import com.thane98.exalt.interfaces.TextDataCreator
import com.thane98.exalt.model.CodegenFunctionData
import com.thane98.exalt.model.CodegenFunctionHeader
import com.thane98.exalt.model.decl.Decl

class V3DeclToFunctionDataConverter(
    textDataCreator: TextDataCreator,
    argSerializer: ArgSerializer,
    codeSerializer: CodeSerializer = V3CodeSerializer(),
) : AbstractDeclToFunctionDataConverter(textDataCreator, argSerializer, codeSerializer) {
    companion object {
        const val HEADER_SIZE = 0x18
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
        // Layout as header -> args -> name -> code
        val argsAddress = if (type == 0) 0 else HEADER_SIZE
        val nameAddress = if (name.isNotEmpty()) HEADER_SIZE + args.size else 0
        val codeAddress = HEADER_SIZE + args.size + name.size
        val header = CodegenFunctionHeader(
            codeAddress,
            nameAddress,
            argsAddress,
            0,
            arity,
            frameSize,
            type,
            argCount
        )
        return CodegenFunctionData(header, name, args, code)
    }

    override fun isAnonymousFunction(name: String): Boolean {
        return !name.contains("::")
    }
}