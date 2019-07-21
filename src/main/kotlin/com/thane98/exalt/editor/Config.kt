package com.thane98.exalt.editor

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import java.util.prefs.Preferences

class Config {
    private val prefs = Preferences.userNodeForPackage(Config::class.java)
    var experimentalMode = SimpleBooleanProperty()
    var showToolBar = SimpleBooleanProperty()
    var showStatusBar = SimpleBooleanProperty()
    var showConsole = SimpleBooleanProperty()
    var theme = SimpleStringProperty()

    init {
        experimentalMode.value = prefs.getBoolean("experimentalMode", false)
        showToolBar.value = prefs.getBoolean("showToolBar", true)
        showStatusBar.value = prefs.getBoolean("showStatusBar", true)
        showConsole.value = prefs.getBoolean("showConsole", false)
        theme.value = prefs.get("theme", "Light")
        if (theme.value != "Light" && theme.value != "Dark")
            theme.value = "Light"
    }

    fun save() {
        prefs.putBoolean("experimentalMode", experimentalMode.value)
        prefs.putBoolean("showToolBar", showToolBar.value)
        prefs.putBoolean("showStatusBar", showStatusBar.value)
        prefs.putBoolean("showConsole", showConsole.value)
        prefs.put("theme", theme.value)
        prefs.flush()
    }
}