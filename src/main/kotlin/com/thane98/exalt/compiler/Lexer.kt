package com.thane98.exalt.compiler

import com.thane98.exalt.error.AggregatedCompilerError
import com.thane98.exalt.error.CompilerError
import com.thane98.exalt.model.CompilerErrorLog
import com.thane98.exalt.model.SourcePosition
import com.thane98.exalt.model.Token
import com.thane98.exalt.model.TokenType
import com.thane98.exalt.model.expr.Literal

class Lexer(
    private val errorLog: CompilerErrorLog,
    private val file: String,
    private val source: List<String>,
    private var lineNumber: Int = 0,
    private var linePos: Int = 0,
) {
    companion object {
        private const val EOF_SIGNAL = 0.toChar()
        private const val MAX_OP_LENGTH = 3
        private val STRING_TO_TYPE = mapOf(
            "script" to TokenType.SCRIPT,
            "event" to TokenType.EVENT,
            "func" to TokenType.FUNC,
            "var" to TokenType.VAR,
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
            ">>" to TokenType.RIGHT_SHIFT,
            "<<" to TokenType.LEFT_SHIFT,
            "&" to TokenType.AMPERSAND,
            "|" to TokenType.BINARY_OR,
            "^" to TokenType.XOR,
            "&&" to TokenType.LOGICAL_AND,
            "||" to TokenType.LOGICAL_OR,
            "~" to TokenType.BINARY_NOT,
            "!" to TokenType.LOGICAL_NOT,
            "++" to TokenType.INCREMENT,
            "--" to TokenType.DECREMENT,
            "=" to TokenType.ASSIGN,
            "+=" to TokenType.ASSIGN_ADD,
            "-=" to TokenType.ASSIGN_SUBTRACT,
            "*=" to TokenType.ASSIGN_MULTIPLY,
            "/=" to TokenType.ASSIGN_DIVIDE,
            "%=" to TokenType.ASSIGN_MODULO,
            "|=" to TokenType.ASSIGN_BINARY_OR,
            "&=" to TokenType.ASSIGN_BINARY_AND,
            "^=" to TokenType.ASSIGN_XOR,
            ">>=" to TokenType.ASSIGN_RIGHT_SHIFT,
            "<<=" to TokenType.ASSIGN_LEFT_SHIFT,
            "+=f" to TokenType.ASSIGN_FLOAT_ADD,
            "-=f" to TokenType.ASSIGN_FLOAT_SUBTRACT,
            "*=f" to TokenType.ASSIGN_FLOAT_MULTIPLY,
            "/=f" to TokenType.ASSIGN_FLOAT_DIVIDE,
            "$" to TokenType.FRAME_REF,
            "$$" to TokenType.GLOBAL_FRAME_REF,
            "," to TokenType.COMMA,
            "(" to TokenType.LPAREN,
            ")" to TokenType.RPAREN,
            "[" to TokenType.LBRACKET,
            "]" to TokenType.RBRACKET,
            "{" to TokenType.LBRACE,
            "}" to TokenType.RBRACE,
            ";" to TokenType.SEMICOLON,
            "->" to TokenType.ARROW,
            "@" to TokenType.AT_SIGN,
        )
    }

    fun scan(): List<Token> {
        // Parse tokens.
        val tokens = mutableListOf<Token>()
        while (!atEnd()) {
            try {
                tokens.addAll(scanNextLine())
            } catch (error: CompilerError) {
                errorLog.addError(error)
            }
            linePos = 0
            lineNumber++
        }

        // Fail if there were any errors.
        if (errorLog.isFailedRun) {
            throw AggregatedCompilerError(errorLog)
        }

        // Add EOF and return.
        tokens.add(Token(TokenType.EOF, SourcePosition(file, "", -1, -1)))
        return tokens
    }

    private fun scanNextLine(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (!endOfLine()) {
            skipWhitespace()
            if (peek() == '#')
                break
            else if (!endOfLine()) {
                tokens.add(scanNextToken())
            }
        }
        return tokens
    }

    private fun scanNextToken(): Token {
        return if (peek() == '"')
            scanString()
        else if (peek().isDigit())
            scanNumber()
        else if (peek().isJavaIdentifierStart() && peek() != '$')
            scanKeywordOrIdentifier()
        else
            scanOperatorOrDelimiter()
    }

    private fun scanKeywordOrIdentifier(): Token {
        val sb = StringBuilder()
        sb.append(next())
        while (peek() != EOF_SIGNAL && peek().isJavaIdentifierPart() || peek() == ':' || peek().toInt() > Byte.MAX_VALUE)
            sb.append(next())
        val str = sb.toString()
        val type = STRING_TO_TYPE[str]
        return if (type != null)
            Token(type, position())
        else
            Token(TokenType.IDENTIFIER, position(), identifier = str)
    }

    private fun scanOperatorOrDelimiter(): Token {
        assert(MAX_OP_LENGTH >= 1)
        for (i in MAX_OP_LENGTH downTo 1) {
            if (linePos + i <= source[lineNumber].length) {
                val str = source[lineNumber].substring(linePos, linePos + i)
                val type = STRING_TO_TYPE[str]
                if (type != null) {
                    linePos += i
                    return Token(type, position())
                }
            }
        }
        throw CompilerError.at(position(), "Unrecognized token starter '${peek()}'")
    }

    private fun scanString(): Token {
        val sb = StringBuilder()
        assert(peek() == '"')
        consume()
        while (peek() != '"') {
            if (endOfLine())
                throw CompilerError.at(position(), "Reached end of line while scanning string.")
            sb.append(next())
        }
        consume()
        return Token(TokenType.STRING, position(), value = Literal.ofString(sb.toString()))
    }

    private fun scanNumber(): Token {
        // Check if number is in hex.
        if (peek() == '0' && peek(1) == 'x')
            return scanHex()

        // Get all digits in the number.
        // Could be floating point, so check for '.' as well.
        val sb = StringBuilder()
        var isFloat = false
        while (peek().isDigit() || peek() == '.') {
            val part = next()
            if (part == '.')
                isFloat = true
            sb.append(part)
        }

        // Create the number literal.
        return if (isFloat) tryParseFloat(sb.toString()) else tryParseInt(sb.toString())
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

    private fun tryParseInt(input: String, base: Int = 10): Token {
        try {
            return Token(TokenType.INT, position(), value = Literal.ofInt(input.toInt(base)))
        } catch (error: NumberFormatException) {
            throw CompilerError.at(position(), "Error parsing float from string.")
        }
    }

    private fun tryParseFloat(input: String): Token {
        try {
            return Token(TokenType.FLOAT, position(), value = Literal.ofFloat(input.toFloat()))
        } catch (error: NumberFormatException) {
            throw CompilerError.at(position(), "Error parsing float from string.")
        }
    }

    private fun position(): SourcePosition {
        return SourcePosition(file, source[lineNumber], lineNumber, linePos)
    }

    private fun skipWhitespace() {
        while (peek() == ' ' || peek() == '\t' || peek() == '\n' || peek() == '\r')
            consume()
    }

    private fun consume() {
        linePos++
    }

    private fun peek(lookahead: Int = 0): Char {
        if (linePos + lookahead >= source[lineNumber].length)
            return EOF_SIGNAL
        return source[lineNumber][linePos + lookahead]
    }

    private fun next(): Char {
        assert(!endOfLine())
        return source[lineNumber][linePos++]
    }

    private fun endOfLine(): Boolean {
        return linePos >= source[lineNumber].length
    }

    private fun atEnd(): Boolean {
        return lineNumber >= source.size
    }
}