package com.thane98.exalt.compiler.transform

import com.thane98.exalt.interfaces.BuiltInFuncallToOpcodeConverter
import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.expr.*
import com.thane98.exalt.model.opcode.ArrAddr
import com.thane98.exalt.model.opcode.PtrAddr
import com.thane98.exalt.model.opcode.VarAddr

class AddrFuncallToOpcodeConverter : BuiltInFuncallToOpcodeConverter {
    override fun toOpcode(funcall: Funcall): Opcode {
        if (funcall.args.size != 1) {
            throw IllegalArgumentException("addr funcall must have exactly 1 argument.")
        }
        return when (val target = funcall.args.first()) {
            is VarRef -> {
                VarAddr(target.symbol.frameId!!)
            }
            is ArrayRef -> {
                ArrAddr(target.symbol.frameId!!)
            }
            is PointerRef -> {
                PtrAddr(target.symbol.frameId!!)
            }
            else -> {
                throw IllegalArgumentException("Target of addr funcall must be a reference.")
            }
        }
    }

    override fun toArgs(funcall: Funcall): List<Expr> {
        if (funcall.args.size != 1) {
            throw IllegalArgumentException("addr funcall must have exactly 1 argument.")
        }
        return when (val target = funcall.args.first()) {
            is VarRef -> listOf()
            is ArrayRef -> listOf(target.index)
            is PointerRef -> listOf(target.index)
            else -> {
                throw IllegalArgumentException("Target of addr funcall must be a reference.")
            }
        }
    }
}