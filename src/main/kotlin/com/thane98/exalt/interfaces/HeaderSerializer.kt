package com.thane98.exalt.interfaces

interface HeaderSerializer {
    /**
     * Generate a script header. We return a mutable list here because
     * this is used a base for serializing the rest of the script components.
     */
    fun serialize(scriptName: String, scriptType: Int): MutableList<Byte>
}