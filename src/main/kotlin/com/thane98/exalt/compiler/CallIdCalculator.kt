package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.DeclVisitor
import com.thane98.exalt.model.Script
import com.thane98.exalt.model.decl.EventDecl
import com.thane98.exalt.model.decl.FunctionDecl
import com.thane98.exalt.model.decl.ScriptDecl

class CallIdCalculator private constructor(): DeclVisitor<Unit> {
    private var nextCallId = 0

    companion object {
        fun calculate(script: Script) {
            val calculator = CallIdCalculator()
            script.contents.forEach { it.accept(calculator) }
        }
    }

    override fun visitScriptDecl(decl: ScriptDecl) {}

    override fun visitEventDecl(decl: EventDecl) {
        nextCallId++
    }

    override fun visitFunctionDecl(decl: FunctionDecl) {
        decl.symbol.callId = nextCallId++
    }
}