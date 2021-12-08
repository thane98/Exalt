package com.thane98.exalt.model

import com.thane98.exalt.compiler.JumpTracker
import com.thane98.exalt.interfaces.TextDataCreator

data class CodegenState(
    val output: MutableList<Byte>,
    val textDataCreator: TextDataCreator,
    val jumpTracker: JumpTracker
)