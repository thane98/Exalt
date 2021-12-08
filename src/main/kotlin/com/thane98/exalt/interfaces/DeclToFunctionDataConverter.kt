package com.thane98.exalt.interfaces

import com.thane98.exalt.model.CodegenFunctionData
import com.thane98.exalt.model.decl.Decl
import com.thane98.exalt.model.decl.ScriptDecl

interface DeclToFunctionDataConverter : DeclVisitor<CodegenFunctionData?> {
    fun convertAllDecls(decls: List<Decl>): List<CodegenFunctionData>

    override fun visitScriptDecl(decl: ScriptDecl): CodegenFunctionData? {
        return null
    }
}