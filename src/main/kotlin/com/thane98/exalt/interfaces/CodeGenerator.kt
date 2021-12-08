package com.thane98.exalt.interfaces

import com.thane98.exalt.model.Script

interface CodeGenerator {
    fun generate(script: Script, scriptName: String): ByteArray
}