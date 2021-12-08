package com.thane98.exalt.ui.model

import com.thane98.exalt.model.Game

data class ScriptOpenRequest(
    val openPath: String,
    val savePath: String,
    val game: Game,
)
