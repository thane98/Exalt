package com.thane98.exalt.model

import com.thane98.exalt.model.expr.Literal

data class Token(
    val type: TokenType,
    val position: SourcePosition,
    val identifier: String? = null,
    val value: Literal? = null,
)
