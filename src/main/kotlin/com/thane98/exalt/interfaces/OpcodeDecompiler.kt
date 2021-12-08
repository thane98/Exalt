package com.thane98.exalt.interfaces

import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult

interface OpcodeDecompiler {
    fun decompile(state: CodeDecompilerState): DecompileStepResult
}