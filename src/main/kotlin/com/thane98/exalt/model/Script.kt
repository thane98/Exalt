package com.thane98.exalt.model

import com.thane98.exalt.model.decl.Decl

data class Script(
    val contents: List<Decl>,
    val game: Game,
    val type: Int,
)
