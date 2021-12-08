package com.thane98.exalt.model

data class CodegenFunctionData(
    val header: CodegenFunctionHeader,
    val name: List<Byte>,
    val args: List<Byte>,
    val code: List<Byte>
)