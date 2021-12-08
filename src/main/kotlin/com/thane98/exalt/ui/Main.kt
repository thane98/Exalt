package com.thane98.exalt.ui

import com.thane98.exalt.ui.controllers.MainWindowController
import com.thane98.exalt.ui.misc.ScriptManagementServer
import com.thane98.exalt.ui.misc.ScriptOpenRequestProcessor
import com.thane98.exalt.ui.misc.StyleUtils
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.text.Font
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.Style

class Main : Application() {

    private lateinit var controller: MainWindowController
    private var scriptManagementServer: ScriptManagementServer? = null

    override fun start(stage: Stage) {
        Font.loadFont(this.javaClass.getResourceAsStream("DejaVuSansMono.ttf"), 14.0)
        val loader = FXMLLoader(this.javaClass.getResource("MainWindow.fxml"))
        val parent: Parent = loader.load()
        val scene = Scene(parent)
        StyleUtils.apply(scene)
        stage.scene = scene
        stage.title = "Exalt"
        stage.show()

        controller = loader.getController() as MainWindowController
        val scriptsModel = controller.scriptsModel
        val configModel = controller.configModel
        if (configModel.enableScriptServer) {
            scriptManagementServer = ScriptManagementServer(
                ScriptOpenRequestProcessor(scriptsModel, stage),
                configModel.scriptServerPort,
            )
        }
    }

    override fun stop() {
        controller.prepareForExit()
        scriptManagementServer?.terminate()
        super.stop()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }
}