package com.thane98.exalt.model

data class CodegenFunctionHeader(
    val codeAddress: Int,
    val nameAddress: Int,
    val argsAddress: Int,
    val parentAddress: Int,
    val arity: Int,
    val frameSize: Int,
    val type: Int,
    val argCount: Int,
)