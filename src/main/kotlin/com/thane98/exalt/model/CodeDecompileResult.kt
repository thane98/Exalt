package com.thane98.exalt.model

import com.thane98.exalt.model.decl.Annotation
import com.thane98.exalt.model.stmt.Block

data class CodeDecompileResult(
    val block: Block,
    val requestedAnnotations: Set<Annotation>,
)