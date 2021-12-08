package com.thane98.exalt.model

import com.thane98.exalt.model.expr.Literal

data class RawFunctionData(
    val codeAddress: Int,
    val parentAddress: Int?,
    val name: String?,
    val args: List<Literal>,
    val arity: Int,
    val type: Int,
    val frameSize: Int,
    val id: Int,
)
