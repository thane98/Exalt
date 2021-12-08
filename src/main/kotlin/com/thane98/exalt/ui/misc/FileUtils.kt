package com.thane98.exalt.ui.misc

import com.thane98.exalt.common.AstPrinter
import com.thane98.exalt.compiler.ExaltCompiler
import com.thane98.exalt.decompiler.ScriptDecompiler
import com.thane98.exalt.model.CompileResult
import com.thane98.exalt.model.Game
import java.nio.file.Files
import java.nio.file.Paths

class FileUtils {
    companion object {
        fun readScript(path: String): String {
            return Files.readString(Paths.get(path))
        }

        fun readAndDecompileScript(path: String, game: Game): String {
            val rawScript = Files.readAllBytes(Paths.get(path))
            val script = ScriptDecompiler.forGame(game).decompile(rawScript)
            return AstPrinter.print(script)
        }

        fun saveScript(script: String, path: String) {
            Files.write(Paths.get(path), script.encodeToByteArray())
        }

        fun compileAndSaveScript(script: String, scriptName: String, path: String): CompileResult {
            val compileResult = ExaltCompiler.compile(script, scriptName)
            Files.write(Paths.get(path), compileResult.rawScript)
            return compileResult
        }
    }
}