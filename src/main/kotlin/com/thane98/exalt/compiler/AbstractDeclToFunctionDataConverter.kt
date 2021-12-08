package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.ArgSerializer
import com.thane98.exalt.interfaces.CodeSerializer
import com.thane98.exalt.interfaces.DeclToFunctionDataConverter
import com.thane98.exalt.interfaces.TextDataCreator
import com.thane98.exalt.model.CodegenFunctionData
import com.thane98.exalt.model.CompilerFeatures
import com.thane98.exalt.model.decl.Decl
import com.thane98.exalt.model.decl.EventDecl
import com.thane98.exalt.model.decl.FunctionDecl
import java.nio.charset.Charset

abstract class AbstractDeclToFunctionDataConverter(
    private val textDataCreator: TextDataCreator,
    private val argSerializer: ArgSerializer,
    private val codeSerializer: CodeSerializer,
) : DeclToFunctionDataConverter {
    companion object {
        private const val FUNCTION_TYPE = 0
        private const val ENCODING = "Shift-JIS"
    }

    override fun convertAllDecls(decls: List<Decl>): List<CodegenFunctionData> {
        return decls.mapNotNull { it.accept(this) }
    }

    override fun visitEventDecl(decl: EventDecl): CodegenFunctionData? {
        // Do args before everything else because they need to go in text data first for accuracy.
        val compilerFeatures = CompilerFeatures.fromAnnotations(decl.annotations)
        val args = argSerializer.serialize(textDataCreator, decl.args)
        return processDecl(
            textDataCreator,
            decl.eventType,
            decl.frameSize + compilerFeatures.framePadding,
            decl.args.size,
            decl.args.size,
            codeSerializer.serialize(textDataCreator, compilerFeatures, decl.body),
            args,
            listOf()
        )
    }

    override fun visitFunctionDecl(decl: FunctionDecl): CodegenFunctionData? {
        val compilerFeatures = CompilerFeatures.fromAnnotations(decl.annotations)
        return processDecl(
            textDataCreator,
            FUNCTION_TYPE,
            decl.frameSize + compilerFeatures.framePadding,
            decl.parameters.size,
            0,
            codeSerializer.serialize(textDataCreator, compilerFeatures, decl.body),
            listOf(),
            serializeName(decl.symbol.name)
        )
    }

    private fun serializeName(name: String): List<Byte> {
        if (isAnonymousFunction(name)) {
            return listOf()
        }
        val nameBytes = name.toByteArray(Charset.forName(ENCODING)).toMutableList()
        nameBytes.add(0)
        return nameBytes
    }

    protected abstract fun isAnonymousFunction(name: String): Boolean

    abstract fun processDecl(
        textDataCreator: TextDataCreator,
        type: Int,
        frameSize: Int,
        arity: Int,
        argCount: Int,
        code: List<Byte>,
        args: List<Byte>,
        name: List<Byte>,
    ): CodegenFunctionData
}