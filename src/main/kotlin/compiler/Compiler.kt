package compiler

import ast.PrettyPrinter
import java.nio.file.Files
import java.nio.file.Paths

class CompileResult {
    val sources = SourceManager()
    val log = Log(sources)
    fun successful(): Boolean {
        return !log.hasErrors()
    }
}

fun compile(source: String, dest: String): CompileResult {
    val result = CompileResult()
    val sources = result.sources
    try {
        val srcPath = sources.addSource(source)!!
        val lexer = Lexer(srcPath, result.log, result.sources)
        val tokens = lexer.tokenize()
        if (result.log.hasErrors())
            return result
        val parser = Parser(tokens, result.log)
        val parseTree = parser.parse()
        if (result.log.hasErrors())
            return result
        val cmb = CodeGenerator3DS.generate(parseTree, dest)
        val outPath = Paths.get(dest)
        Files.write(outPath, cmb.toByteArray())
    } catch (error: CompileError) {
        result.log.logError(error)
    }
    return result
}