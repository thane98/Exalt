package com.thane98.exalt.testScripts

import com.thane98.exalt.common.AstPrinter
import com.thane98.exalt.compiler.ExaltCompiler
import com.thane98.exalt.decompiler.ScriptDecompiler
import com.thane98.exalt.error.AggregatedCompilerError
import com.thane98.exalt.model.Game
import com.thane98.exalt.model.Script
import com.thane98.exalt.model.VersionInfo
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

data class TestState(
    val successes: MutableList<String> = mutableListOf(),
    val mismatches: MutableList<String> = mutableListOf(),
    val errors: MutableList<String> = mutableListOf(),
)

class TestHarness(
    private val testDirectory: String,
    private val game: Game,
    private val state: TestState = TestState()
) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val harness = TestHarness(
                "",
                Game.FE11,
            )
            harness.execute()
        }
    }

    fun execute() {
        val files = collectTestFiles(testDirectory)
        executeTests(files)
        displayResults()
    }

    private fun collectTestFiles(path: String): List<String> {
        return Files.find(Paths.get(path), 999, { _, bfa -> bfa.isRegularFile })
            .map { it.toString() }
            .collect(Collectors.toList())
    }

    private fun executeTests(files: List<String>) {
        val decompiler = ScriptDecompiler.forGame(game)
        for (file in files) {
            try {
                val isV3 = game in setOf(Game.FE13, Game.FE14, Game.FE15)
                val contents = Files.readAllBytes(Paths.get(file))
                val script = decompiler.decompile(contents)
                val scriptName = Paths.get(file).fileName.toString()
                if (isV3) {
                    executeV3Test(script, file, scriptName, contents)
                } else {
                    executeV1OrV2Test(script, file, scriptName, contents)
                }
            } catch (ex: AggregatedCompilerError) {
                println(ex.errors())
                state.errors.add(file)
            } catch (ex: Exception) {
                ex.printStackTrace()
                state.errors.add(file)
            }
        }
    }

    private fun executeV3Test(script: Script, file: String, scriptName: String, rawScript: ByteArray) {
        val compiled = ExaltCompiler.compile(AstPrinter.print(script), scriptName)
        if (!compiled.rawScript.contentEquals(rawScript)) {
            state.mismatches.add(file)
        } else {
            state.successes.add(file)
        }
    }

    private fun executeV1OrV2Test(script: Script, file: String, scriptName: String, rawScript: ByteArray) {
        val hardCodedTextData = V1TextOffsetsExtractor(VersionInfo.v2()).extract(rawScript)
        val compiled =
            ExaltCompiler.compileWithHardCodedTextData(AstPrinter.print(script), scriptName, hardCodedTextData)
        if (!compiled.rawScript.contentEquals(rawScript)) {
            state.mismatches.add(file)
        } else {
            state.successes.add(file)
        }
    }

    private fun displayResults() {
        val total = state.successes.size + state.errors.size + state.mismatches.size
        val successRate = state.successes.size.toDouble() / total.toDouble() * 100.0
        println("Successes: ${state.successes.size}")
        println("Mismatches: ${state.mismatches.size}")
        println("Errors: ${state.errors.size}")
        println("Success Rate: $successRate%")
        println()
        println("Mismatches:")
        state.mismatches.forEach { println("- " + Paths.get(it).fileName) }
        println()
        println("Errors:")
        state.errors.forEach { println("- " + Paths.get(it).fileName) }
    }
}