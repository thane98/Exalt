package com.thane98.exalt.model.decl

data class Annotation(
    val name: String,
    val args: List<String> = listOf(),
)