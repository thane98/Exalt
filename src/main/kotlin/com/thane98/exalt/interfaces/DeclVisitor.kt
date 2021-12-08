package com.thane98.exalt.interfaces

import com.thane98.exalt.model.decl.EventDecl
import com.thane98.exalt.model.decl.FunctionDecl
import com.thane98.exalt.model.decl.ScriptDecl

interface DeclVisitor<T> {
    fun visitScriptDecl(decl: ScriptDecl): T
    fun visitEventDecl(decl: EventDecl): T
    fun visitFunctionDecl(decl: FunctionDecl): T
}