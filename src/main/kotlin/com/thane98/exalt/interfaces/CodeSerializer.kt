package com.thane98.exalt.interfaces

import com.thane98.exalt.model.CompilerFeatures
import com.thane98.exalt.model.stmt.Stmt

interface CodeSerializer {
    fun serialize(textDataCreator: TextDataCreator, features: CompilerFeatures, code: Stmt): List<Byte>
}