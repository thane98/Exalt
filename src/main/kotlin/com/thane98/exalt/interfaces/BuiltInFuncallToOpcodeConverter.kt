package com.thane98.exalt.interfaces

import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.expr.Funcall

interface BuiltInFuncallToOpcodeConverter {
    fun toOpcode(funcall: Funcall): Opcode

    fun toArgs(funcall: Funcall): List<Expr> {
        return funcall.args
    }
}