package com.thane98.exalt.interfaces

import com.thane98.exalt.model.CompilerFeatures
import com.thane98.exalt.model.stmt.Stmt

interface AstToOpcodeConverter {
    fun convert(stmt: Stmt, features: CompilerFeatures): List<Opcode>
}