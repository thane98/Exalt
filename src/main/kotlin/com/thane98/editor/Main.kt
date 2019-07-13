package com.thane98.editor

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import jfxtras.styles.jmetro8.JMetro

class Main : Application() {
    private lateinit var controller: MainWindowController

    override fun start(stage: Stage) {
        val loader = FXMLLoader(this.javaClass.getResource("MainWindow.fxml"))
        val parent: Parent = loader.load()
        val scene = Scene(parent)
        controller = loader.getController() as MainWindowController
        stage.scene = scene
        stage.title = "Exalt"
        stage.show()
    }

    override fun stop() {
        controller.saveAndCloseTabs()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }
}