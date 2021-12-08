package com.thane98.exalt.model

data class ParserFeatures(
    var simplifyNegativeInts: Boolean,
) {
    companion object {
        fun forGame(game: Game): ParserFeatures {
            return when (game) {
                Game.FE10 -> ParserFeatures(false)
                Game.FE11, Game.FE12, Game.FE13, Game.FE14, Game.FE15 -> ParserFeatures(true)
            }
        }
    }
}