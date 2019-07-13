package com.thane98.compiler

import com.thane98.common.TokenType
import java.lang.NumberFormatException

class Lexer(
    private val srcPath: String,
    private val log: Log,
    private val sourceManager: SourceManager
) {
    private val source = sourceManager.sourceFor(srcPath)
    private var line = 0
    private var start = 0
    private var linePos = 0

    companion object {
        private const val MAX_OP_LENGTH = 3
        private val STRING_TO_TYPE = hashMapOf(
            "event" to TokenType.EVENT,
            "func" to TokenType.FUNC,
            "const" to TokenType.CONST,
            "var" to TokenType.VAR,
            "let" to TokenType.LET,
            "for" to TokenType.FOR,
            "while" to TokenType.WHILE,
            "if" to TokenType.IF,
            "else" to TokenType.ELSE,
            "match" to TokenType.MATCH,
            "return" to TokenType.RETURN,
            "yield" to TokenType.YIELD,
            "array" to TokenType.ARRAY,
            "goto" to TokenType.GOTO,
            "label" to TokenType.LABEL,
            "fix" to TokenType.FIX,
            "float" to TokenType.FLOAT,
            "+" to TokenType.PLUS,
            "-" to TokenType.MINUS,
            "*" to TokenType.TIMES,
            "/" to TokenType.DIVIDE,
            "%" to TokenType.MODULO,
            "+f" to TokenType.FPLUS,
            "-f" to TokenType.FMINUS,
            "*f" to TokenType.FTIMES,
            "/f" to TokenType.FDIVIDE,
            "==" to TokenType.EQ,
            "!=" to TokenType.NE,
            "<" to TokenType.LT,
            "<=" to TokenType.LE,
            ">" to TokenType.GT,
            ">=" to TokenType.GE,
            "==f" to TokenType.FEQ,
            "!=f" to TokenType.FNE,
            "<f" to TokenType.FLT,
            "<=f" to TokenType.FLE,
            ">f" to TokenType.FGT,
            ">=f" to TokenType.FGE,
            ">>" to TokenType.RS,
            "<<" to TokenType.LS,
            "&" to TokenType.BAND,
            "|" to TokenType.BOR,
            "^" to TokenType.XOR,
            "&&" to TokenType.AND,
            "||" to TokenType.OR,
            "~" to TokenType.BNOT,
            "!" to TokenType.NOT,
            "++" to TokenType.INC,
            "--" to TokenType.DEC,
            "=" to TokenType.ASSIGN,
            "+=" to TokenType.ASSIGN_PLUS,
            "-=" to TokenType.ASSIGN_MINUS,
            "*=" to TokenType.ASSIGN_TIMES,
            "/=" to TokenType.ASSIGN_DIVIDE,
            "+=f" to TokenType.ASSIGN_FPLUS,
            "-=f" to TokenType.ASSIGN_FMINUS,
            "*=f" to TokenType.ASSIGN_FTIMES,
            "/=f" to TokenType.ASSIGN_FDIVIDE,
            "$" to TokenType.FRAME_REF,
            "," to TokenType.COMMA,
            "(" to TokenType.LPAREN,
            ")" to TokenType.RPAREN,
            "[" to TokenType.LBRACKET,
            "]" to TokenType.RBRACKET,
            "{" to TokenType.LBRACE,
            "}" to TokenType.RBRACE,
            ";" to TokenType.SEMICOLON,
            "->" to TokenType.ARROW
        )
    }

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        reset()
        while (!atEnd()) {
            try {
                tokenizeNextLine(tokens)
            } catch (error: CompileError) {
                log.logError(error)
            }
            linePos = 0
            line++
        }
        tokens.add(Token(TokenType.EOF, SourcePosition(srcPath, -1, -1)))
        return tokens
    }

    private fun reset() {
        line = 0
        start = 0
        linePos = 0
    }

    private fun tokenizeNextLine(tokens: MutableList<Token>) {
        // Handle includes separately.
        if (source[line].startsWith("include")) {
            handleInclude(tokens)
            return
        }

        // Turn the line into tokens.
        while (!endOfLine()) {
            skipWhitespace()
            if (peek() == '#')
                return
            else if (!endOfLine()) {
                start = linePos
                tokens.add(nextToken())
            }
        }
    }

    private fun handleInclude(tokens: MutableList<Token>) {
        // Get the path string.
        linePos = source[line].indexOf('"')
        if (linePos == -1)
            throw CompileError("Expected path string after include.", position())
        val str = scanString()

        // Try to open the file and scan its contents.
        // If tgtPath is null, the file was already included.
        assert(str.literal is String)
        try {
            val tgtPath = sourceManager.addSource(str.literal as String)
            if (tgtPath != null) {
                tokens.addAll(Lexer(tgtPath, log, sourceManager).tokenize())
                assert(tokens.last().type == TokenType.EOF)
                tokens.removeAt(tokens.lastIndex) // Get rid of the EOF
                sourceManager.removeLastSearchPath()
            }
        } catch (error: CompileError) {
            throw CompileError(error.msg, position())
        }
    }

    private fun skipWhitespace() {
        while (peek() == ' ' || peek() == '\t' || peek() == '\n' || peek() == '\r')
            consume()
    }

    private fun nextToken(): Token {
        if (peek() == '"')
            return scanString()
        if (peek().isDigit())
            return scanNumber()
        if (peek().isJavaIdentifierStart() && peek() != '$')
            return scanIdentifierOrKeyword()
        return scanOperatorOrDelimiter() ?: throw CompileError("Unrecognized token.", position())
    }

    private fun scanString(): Token {
        val sb = StringBuilder()
        assert(peek() == '"')
        consume()
        while (peek() != '"') {
            if (endOfLine())
                throw CompileError("Reached end of line while scanning string.", position())
            sb.append(next())
        }
        consume()
        return Token(TokenType.STRING, position(), sb.toString())
    }

    private fun scanNumber(): Token {
        // Check if number is in hex.
        assert(peek() == '0')
        if (peek() == '0' && peek(1) == 'x')
            return scanHex()

        // Get all digits in the number.
        // Could be floating point, so check for '.' as well.
        val sb = StringBuilder()
        var isReal = false
        while (peek().isDigit() || peek() == '.') {
            val part = next()
            if (part == '.')
                isReal = true
            sb.append(part)
        }

        // Create the number literal.
        if (isReal)
            return tryParseFloat(sb.toString())
        return tryParseInt(sb.toString())
    }

    private fun tryParseInt(input: String, base: Int = 10): Token {
        try {
            return Token(TokenType.INT, position(), input.toInt(base))
        } catch (error: NumberFormatException) {
            throw CompileError("Error parsing integer from string.", position())
        }
    }

    private fun tryParseFloat(input: String): Token {
        try {
            return Token(TokenType.REAL, position(), input.toFloat())
        } catch (error: NumberFormatException) {
            throw CompileError("Error parsing float from string.", position())
        }
    }

    private fun scanHex(): Token {
        assert(peek() == '0' && peek(1) == 'x')
        val sb = StringBuilder()
        linePos += 2 // Skip prefix '0x'
        while (nextIsHex())
            sb.append(next())
        return tryParseInt(sb.toString(), 16)
    }

    private fun nextIsHex(): Boolean {
        val tgt = peek().toLowerCase()
        return tgt.isDigit() || tgt in 'a'..'f'
    }

    private fun scanIdentifierOrKeyword(): Token {
        val sb = StringBuilder()
        sb.append(next())
        while (peek().isJavaIdentifierPart() || peek() == ':' || peek().toInt() > Byte.MAX_VALUE)
            sb.append(next())
        val str = sb.toString()
        val type = STRING_TO_TYPE[str]
        return if (type != null)
            Token(type, position())
        else
            Token(TokenType.IDENTIFIER, position(), str)
    }

    private fun scanOperatorOrDelimiter(): Token? {
        assert(MAX_OP_LENGTH >= 1)
        for(i in MAX_OP_LENGTH downTo 1) {
            if (linePos + i <= source[line].length) {
                val str = source[line].substring(linePos, linePos + i)
                val type = STRING_TO_TYPE[str]
                if (type != null) {
                    linePos += i
                    return Token(type, position())
                }
            }
        }
        return null
    }

    private fun consume() {
        linePos++
    }

    private fun position(): SourcePosition {
        return SourcePosition(srcPath, line + 1, start)
    }

    private fun peek(lookahead: Int = 0): Char {
        if (linePos + lookahead >= source[line].length)
            return 0.toChar()
        return source[line][linePos + lookahead]
    }

    private fun next(): Char {
        assert(!endOfLine())
        return source[line][linePos++]
    }

    private fun endOfLine(): Boolean {
        return linePos >= source[line].length
    }

    private fun atEnd(): Boolean {
        return line >= source.size
    }
}