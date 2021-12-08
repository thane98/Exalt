package com.thane98.exalt.compiler

import com.thane98.exalt.model.CompileResult
import com.thane98.exalt.model.CompilerErrorLog
import com.thane98.exalt.model.HardCodedTextData

class ExaltCompiler {
    companion object {
        fun compile(script: String, scriptName: String): CompileResult {
            val errorLog = CompilerErrorLog()
            val lexer = Lexer(errorLog, scriptName, script.split("\r\n", "\n", "\r"))
            val parser = Parser(errorLog, lexer.scan())
            val ast = parser.parse()
            PostParseProcessor.process(ast)
            val codeGenerator = CodeGeneratorFactory.buildGeneratorForScript(ast)
            val rawScript = codeGenerator.generate(ast, scriptName)
            return CompileResult(rawScript, ast.game, ast.type)
        }

        fun compileWithHardCodedTextData(
            script: String,
            scriptName: String,
            hardCodedTextData: HardCodedTextData
        ): CompileResult {
            val errorLog = CompilerErrorLog()
            val lexer = Lexer(errorLog, scriptName, script.split("\r\n", "\n", "\r"))
            val parser = Parser(errorLog, lexer.scan())
            val ast = parser.parse()
            PostParseProcessor.process(ast)
            val codeGenerator =
                CodeGeneratorFactory.buildGeneratorForScript(ast, HardCodedOffsetsTextDataCreator(hardCodedTextData))
            val rawScript = codeGenerator.generate(ast, scriptName)
            return CompileResult(rawScript, ast.game, ast.type)
        }
    }
}