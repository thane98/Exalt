package editor

import javafx.beans.property.SimpleBooleanProperty
import java.util.prefs.Preferences

class Config {
    private val prefs = Preferences.userNodeForPackage(Config::class.java)
    var experimentalMode = SimpleBooleanProperty()

    init {
        experimentalMode.value = prefs.getBoolean("experimentalMode", false)
    }

    fun save() {
        prefs.putBoolean("experimentalMode", experimentalMode.value)
        prefs.flush()
    }
}