package com.thane98.exalt.common

enum class Precedence {
    NONE,
    ASSIGNMENT,
    OR,
    AND,
    BOR,
    XOR,
    BAND,
    EQUALITY,
    COMPARISON,
    SHIFT,
    ADDITION,
    MULTIPLICATION,
    PREFIX,
    POSTFIX,
    LITERAL
}

enum class TokenType {
    // Keywords
    EVENT,
    FUNC, CONST, VAR, LET, FOR, WHILE, IF, ELSE,
    MATCH, RETURN, YIELD, ARRAY, GOTO, LABEL, FIX, FLOAT,

    // Basic Operators
    PLUS,
    MINUS, TIMES, DIVIDE, MODULO,
    FPLUS, FMINUS, FTIMES, FDIVIDE,
    EQ, NE, LT, LE, GT, GE,
    FEQ, FNE, FLT, FLE, FGT, FGE,
    RS, LS, BAND, BOR, XOR, BNOT, NOT,
    AND, OR, INC, DEC,

    // Assignment Operators
    ASSIGN,
    ASSIGN_PLUS, ASSIGN_MINUS, ASSIGN_TIMES, ASSIGN_DIVIDE,
    ASSIGN_FPLUS, ASSIGN_FMINUS, ASSIGN_FTIMES, ASSIGN_FDIVIDE,

    // Misc Operators
    FRAME_REF,

    // Delimiters
    COMMA,
    LPAREN, RPAREN, LBRACKET, RBRACKET, LBRACE, RBRACE,
    SEMICOLON, EOF, ARROW,

    // Literals
    INT,
    REAL, STRING, IDENTIFIER;

    fun isAssignment(): Boolean {
        return when (this) {
            ASSIGN -> true
            ASSIGN_PLUS -> true
            ASSIGN_MINUS -> true
            ASSIGN_TIMES -> true
            ASSIGN_DIVIDE -> true
            ASSIGN_FPLUS -> true
            ASSIGN_FMINUS -> true
            ASSIGN_FTIMES -> true
            ASSIGN_FDIVIDE -> true
            else -> false
        }
    }

    fun precedence(): Precedence {
        when (this) {
            FIX -> return Precedence.PREFIX
            FLOAT -> return Precedence.PREFIX
            PLUS -> return Precedence.ADDITION
            MINUS -> return Precedence.ADDITION
            TIMES -> return Precedence.MULTIPLICATION
            DIVIDE -> return Precedence.MULTIPLICATION
            MODULO -> return Precedence.MULTIPLICATION
            FPLUS -> return Precedence.ADDITION
            FMINUS -> return Precedence.ADDITION
            FTIMES -> return Precedence.MULTIPLICATION
            FDIVIDE -> return Precedence.MULTIPLICATION
            EQ -> return Precedence.EQUALITY
            NE -> return Precedence.EQUALITY
            LT -> return Precedence.COMPARISON
            LE -> return Precedence.COMPARISON
            GT -> return Precedence.COMPARISON
            GE -> return Precedence.COMPARISON
            FEQ -> return Precedence.EQUALITY
            FNE -> return Precedence.EQUALITY
            FLT -> return Precedence.COMPARISON
            FLE -> return Precedence.COMPARISON
            FGT -> return Precedence.COMPARISON
            FGE -> return Precedence.COMPARISON
            RS -> return Precedence.SHIFT
            LS -> return Precedence.SHIFT
            BAND -> return Precedence.BAND
            BOR -> return Precedence.BOR
            XOR -> return Precedence.XOR
            BNOT -> return Precedence.PREFIX
            NOT -> return Precedence.PREFIX
            AND -> return Precedence.AND
            OR -> return Precedence.OR
            INC -> return Precedence.POSTFIX
            DEC -> return Precedence.POSTFIX
            ASSIGN -> return Precedence.ASSIGNMENT
            ASSIGN_PLUS -> return Precedence.ASSIGNMENT
            ASSIGN_MINUS -> return Precedence.ASSIGNMENT
            ASSIGN_TIMES -> return Precedence.ASSIGNMENT
            ASSIGN_DIVIDE -> return Precedence.ASSIGNMENT
            ASSIGN_FPLUS -> return Precedence.ASSIGNMENT
            ASSIGN_FMINUS -> return Precedence.ASSIGNMENT
            ASSIGN_FTIMES -> return Precedence.ASSIGNMENT
            ASSIGN_FDIVIDE -> return Precedence.ASSIGNMENT
            FRAME_REF -> return Precedence.PREFIX
            INT -> return Precedence.LITERAL
            REAL -> return Precedence.LITERAL
            STRING -> return Precedence.LITERAL
            IDENTIFIER -> return Precedence.LITERAL
            else -> return Precedence.NONE
        }
    }

    override fun toString(): String {
        when (this) {
            PLUS -> return "+"
            MINUS -> return "-"
            TIMES -> return "*"
            DIVIDE -> return "/"
            MODULO -> return "%"
            FPLUS -> return "+f"
            FMINUS -> return "-f"
            FTIMES -> return "*f"
            FDIVIDE -> return "/f"
            EQ -> return "=="
            NE -> return "!="
            LT -> return "<"
            LE -> return "<="
            GT -> return ">"
            GE -> return ">="
            FEQ -> return "==f"
            FNE -> return "!=f"
            FLT -> return "<f"
            FLE -> return "<=f"
            FGT -> return ">f"
            FGE -> return ">=f"
            RS -> return ">>"
            LS -> return "<<"
            BAND -> return "&"
            BOR -> return "|"
            XOR -> return "^"
            BNOT -> return "~"
            NOT -> return "!"
            AND -> return "&&"
            OR -> return "||"
            INC -> return "++"
            DEC -> return "--"
            ASSIGN -> return "="
            ASSIGN_PLUS -> return "+="
            ASSIGN_MINUS -> return "-="
            ASSIGN_TIMES -> return "*="
            ASSIGN_DIVIDE -> return "/="
            ASSIGN_FPLUS -> return "+=f"
            ASSIGN_FMINUS -> return "-=f"
            ASSIGN_FTIMES -> return "*=f"
            ASSIGN_FDIVIDE -> return "/=f"
            FRAME_REF -> return "$"
            COMMA -> return ","
            LPAREN -> return "("
            RPAREN -> return ")"
            LBRACKET -> return "["
            RBRACKET -> return "]"
            LBRACE -> return "{"
            RBRACE -> return "}"
            SEMICOLON -> return ";"
            ARROW -> return "->"
            else -> return super.toString().toLowerCase()
        }
    }
}

