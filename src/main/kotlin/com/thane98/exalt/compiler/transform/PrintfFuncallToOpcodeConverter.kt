package com.thane98.exalt.compiler.transform

import com.thane98.exalt.interfaces.BuiltInFuncallToOpcodeConverter
import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.expr.Funcall
import com.thane98.exalt.model.opcode.Printf

class PrintfFuncallToOpcodeConverter : BuiltInFuncallToOpcodeConverter {
    override fun toOpcode(funcall: Funcall): Opcode {
        return Printf(funcall.args.size.toByte())
    }
}