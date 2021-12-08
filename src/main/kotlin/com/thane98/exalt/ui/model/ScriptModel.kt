package com.thane98.exalt.ui.model

import com.thane98.exalt.model.Game
import com.thane98.exalt.ui.misc.FileUtils
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import java.nio.file.Paths

class ScriptModel {
    val sourceFilePathProperty: StringProperty = SimpleStringProperty("")
    var sourceFilePath: String
        get() = sourceFilePathProperty.value
        set(value) { sourceFilePathProperty.value = value }
    val compiledFilePathProperty: StringProperty = SimpleStringProperty("")
    var compiledFilePath: String
        get() = compiledFilePathProperty.value
        set(value) { compiledFilePathProperty.value = value }
    val contentsProperty: StringProperty = SimpleStringProperty("")
    var contents: String
        get() = contentsProperty.value
        set(value) { contentsProperty.value = value }
    val sourceFileNameProperty: StringProperty = SimpleStringProperty("{no name}")
    val sourceFileName: String
        get() = sourceFileNameProperty.value
    val compiledFileNameProperty: StringProperty = SimpleStringProperty("{no name}")
    val compiledFileName: String
        get() = compiledFileNameProperty.value

    companion object {
        fun fromFile(filePath: String, game: Game): ScriptModel {
            return if (filePath.endsWith(".exl")) {
                fromSourceFile(filePath)
            } else {
                fromCompiledFile(filePath, game)
            }
        }

        fun fromSourceFile(sourceFilePath: String): ScriptModel {
            val contents = FileUtils.readScript(sourceFilePath)
            val scriptModel = ScriptModel()
            scriptModel.sourceFilePathProperty.value = sourceFilePath
            scriptModel.contentsProperty.value = contents
            return scriptModel
        }

        fun fromCompiledFile(compiledFilePath: String, game: Game): ScriptModel {
            val contents = FileUtils.readAndDecompileScript(compiledFilePath, game)
            val scriptModel = ScriptModel()
            scriptModel.compiledFilePathProperty.value = compiledFilePath
            scriptModel.contentsProperty.value = contents
            return scriptModel
        }
    }

    init {
        sourceFilePathProperty.addListener {
                _ -> sourceFileNameProperty.value = tryGetFileName(sourceFilePathProperty.value) ?: ""
        }
        compiledFilePathProperty.addListener {
                _ -> compiledFileNameProperty.value = tryGetFileName(compiledFilePathProperty.value) ?: ""
        }
    }

    fun canSave(): Boolean {
        return !sourceFilePathProperty.value.isNullOrBlank()
    }

    fun canCompile(): Boolean {
        return !compiledFilePathProperty.value.isNullOrBlank()
    }

    private fun tryGetFileName(value: String): String? {
        return try {
            Paths.get(value).fileName.toString()
        } catch(_: Exception) {
            null
        }
    }
}