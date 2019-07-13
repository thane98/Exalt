package com.thane98.compiler

import com.thane98.ast.Block
import java.nio.file.Files
import java.nio.file.Paths

class CompileResult {
    val sources = SourceManager()
    val log = Log(sources)
    var parseTree: Block? = null

    fun successful(): Boolean {
        return !log.hasErrors()
    }
}

fun compileFromInMemoryScript(source: String, dest: String): CompileResult {
    val result = CompileResult()
    result.sources.addInMemorySource(source)
    performParse(result, SourceManager.IN_MEMORY_SOURCE_PATH)
    generateAndWriteCmb(result.parseTree!!, dest)
    return result
}

fun compile(source: String, dest: String): CompileResult {
    val result = CompileResult()
    try {
        val srcPath = result.sources.addSource(source)!!
        performParse(result, srcPath)
        generateAndWriteCmb(result.parseTree!!, dest)
    } catch (error: CompileError) {
        result.log.logError(error)
    }
    return result
}

private fun performParse(out: CompileResult, sourcePath: String) {
    try {
        val lexer = Lexer(sourcePath, out.log, out.sources)
        val tokens = lexer.tokenize()
        if (out.log.hasErrors())
            return
        val parser = Parser(tokens, out.log)
        out.parseTree = parser.parse()
    } catch (error: CompileError) {
        out.log.logError(error)
    }
}

private fun generateAndWriteCmb(script: Block, dest: String) {
    val cmb = CodeGenerator3DS.generate(script, dest)
    Files.write(Paths.get(dest), cmb.toByteArray())
}