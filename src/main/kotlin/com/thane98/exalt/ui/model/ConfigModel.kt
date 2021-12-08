package com.thane98.exalt.ui.model

import com.thane98.exalt.model.Game
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import java.util.prefs.Preferences

class ConfigModel {
    val showStatusBarProperty = SimpleBooleanProperty(true)
    var showStatusBar: Boolean
        get() = showStatusBarProperty.value
        set(value) { showStatusBarProperty.value = value }
    val showToolBarProperty = SimpleBooleanProperty(true)
    var showToolBar: Boolean
        get() = showToolBarProperty.value
        set(value) { showToolBarProperty.value = value }
    val showConsoleProperty = SimpleBooleanProperty(false)
    var showConsole: Boolean
        get() = showConsoleProperty.value
        set(value) { showConsoleProperty.value = value }
    val gameProperty = SimpleObjectProperty(Game.FE14)
    var game: Game
        get() = gameProperty.value
        set(value) { gameProperty.value = value }
    val enableScriptServerProperty = SimpleBooleanProperty(true)
    var enableScriptServer: Boolean
        get() = enableScriptServerProperty.value
        set(value) { enableScriptServerProperty.value = value }
    val scriptServerPortProperty = SimpleIntegerProperty(30000)
    var scriptServerPort: Int
        get() = scriptServerPortProperty.value
        set(value) { scriptServerPortProperty.value = value }
    val translateFunctionNamesProperty = SimpleBooleanProperty(true)
    var translateFunctionNames: Boolean
        get() = translateFunctionNamesProperty.value
        set(value) { translateFunctionNamesProperty.value = value }

    companion object {
        fun loadFromPreferences(): ConfigModel {
            val prefs = Preferences.userNodeForPackage(this::class.java)
            val model = ConfigModel()
            model.showStatusBar = prefs.getBoolean("showStatusBar", true)
            model.showToolBar = prefs.getBoolean("showToolBar", true)
            model.showConsole = prefs.getBoolean("showConsole", false)
            model.enableScriptServer = prefs.getBoolean("enableScriptServer", true)
            model.scriptServerPort = prefs.getInt("scriptServerPort", 30000)
            model.translateFunctionNames = prefs.getBoolean("translateFunctionNames", true)
            try {
                model.game = Game.valueOf(prefs.get("game", Game.FE14.name))
            } catch (ex: Exception) {
                model.game = Game.FE14
                ex.printStackTrace()
            }
            return model
        }
    }

    fun saveToPreferences() {
        val prefs = Preferences.userNodeForPackage(this::class.java)
        prefs.putBoolean("showStatusBar", showStatusBar)
        prefs.putBoolean("showToolBar", showToolBar)
        prefs.putBoolean("showConsole", showConsole)
        prefs.put("game", game.toString())
        prefs.putBoolean("enableScriptServer", enableScriptServer)
        prefs.putInt("scriptServerPort", scriptServerPort)
        prefs.putBoolean("translateFunctionNames", translateFunctionNames)
    }
}