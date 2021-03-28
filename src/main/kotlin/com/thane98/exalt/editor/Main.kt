package com.thane98.exalt.editor

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.Style

class Main : Application() {
    private lateinit var controller: MainWindowController

    override fun start(stage: Stage) {
        val loader = FXMLLoader(this.javaClass.getResource("MainWindow.fxml"))
        val parent: Parent = loader.load()
        val scene = Scene(parent)
        val jmetro = JMetro(Style.DARK)
        jmetro.scene = scene
        scene.stylesheets.add(this.javaClass.getResource("styles-common.css").toExternalForm())
        scene.stylesheets.add(this.javaClass.getResource("styles-dark.css").toExternalForm())
        controller = loader.getController() as MainWindowController
        scene.setOnKeyPressed { keyEvent -> controller.handleCancel(keyEvent) }
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