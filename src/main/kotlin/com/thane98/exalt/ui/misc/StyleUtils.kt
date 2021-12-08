package com.thane98.exalt.ui.misc

import com.thane98.exalt.ui.Main
import javafx.scene.Scene
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.Style

class StyleUtils {
    companion object {
        fun apply(scene: Scene) {
            val jmetro = JMetro(Style.DARK)
            jmetro.scene = scene
            scene.stylesheets.add(Main::class.java.getResource("styles.css").toExternalForm())
        }
    }
}