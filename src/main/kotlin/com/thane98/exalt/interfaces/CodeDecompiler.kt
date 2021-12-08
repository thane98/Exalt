package com.thane98.exalt.interfaces

import com.thane98.exalt.model.CodeDecompileResult
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.stmt.Stmt

interface CodeDecompiler {
    fun decompile(address: Int): CodeDecompileResult
    fun decompileUntil(state: CodeDecompilerState, offset: Int)
    fun decompileBlock(state: CodeDecompilerState, offset: Int)
    fun decompileNextStatement(state: CodeDecompilerState): Stmt?
    fun decompileNext(state: CodeDecompilerState): DecompileStepResult
}