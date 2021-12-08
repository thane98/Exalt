package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.DeclVisitor
import com.thane98.exalt.model.Script
import com.thane98.exalt.model.decl.EventDecl
import com.thane98.exalt.model.decl.FunctionDecl
import com.thane98.exalt.model.decl.ScriptDecl

class NamedFunctionFinder private constructor(private val isV3: Boolean) : DeclVisitor<String?> {
    companion object {
        fun findAllNamedFunctions(script: Script, isV3: Boolean): Set<String> {
            val finder = NamedFunctionFinder(isV3)
            return script.contents.mapNotNull { it.accept(finder) }.toSet()
        }
    }

    override fun visitScriptDecl(decl: ScriptDecl): String? {
        return null
    }

    override fun visitEventDecl(decl: EventDecl): String? {
        return null
    }

    override fun visitFunctionDecl(decl: FunctionDecl): String? {
        return if (isV3 && decl.symbol.name.contains("::")) {
            decl.symbol.name.split("::").last()
        } else {
            null
        }
    }
}