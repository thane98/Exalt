package com.thane98.exalt.common

import java.lang.IllegalArgumentException
import java.nio.charset.Charset

class Format3DS {
    companion object {
        val EVENT_TABLE_POINTER = 0x1C
        val TEXT_DATA_POINTER = 0x20
        val EVENT_HEADER_SIZE = 0x18
        val ENCODING = Charset.forName("shift-jis")
    }
}

enum class Opcode3DS(val opcode: Byte) {
    VAR_GET(0x1),
    ARR_GET(0x3),
    PTR_GET(0x5),
    VAR_LOAD(0x7),
    ARR_LOAD(0x9),
    PTR_LOAD(0xB),
    LOAD_BYTE(0x19),
    LOAD_SHORT(0x1A),
    LOAD_INT(0x1B),
    LOAD_TEXT_BYTE(0x1C),
    LOAD_TEXT_SHORT(0x1D),
    LOAD_TEXT_INT(0x1E),
    LOAD_FLOAT(0x1F),
    COPY_AND_DEREFERENCE_TOP(0x20),
    CONSUME_TOP(0x21),
    COMPLETE_ASSIGN(0x23),
    FIX(0x24),
    FLOAT(0x25),
    PLUS(0x26),
    FPLUS(0x27),
    MINUS(0x28),
    FMINUS(0x29),
    TIMES(0x2A),
    FTIMES(0x2B),
    DIVIDE(0x2C),
    FDIVIDE(0x2D),
    MODULO(0x2E),
    NEGATE_INT(0x2F),
    NEGATE_REAL(0x30),
    BNOT(0x31),
    NOT(0x32),
    BOR(0x33),
    BAND(0x34),
    XOR(0x35),
    LS(0x36),
    RS(0x37),
    EQ(0x38),
    FEQ(0x39),
    NE(0x3B),
    FNE(0x3C),
    LT(0x3E),
    FLT(0x3F),
    LE(0x40),
    FLE(0x41),
    GT(0x42),
    FGT(0x43),
    GE(0x44),
    FGE(0x45),
    LOCAL_CALL(0x46),
    GLOBAL_CALL(0x47),
    SET_RETURN(0x48),
    JUMP(0x49),
    JUMP_NOT_ZERO(0x4A),
    OR(0x4B),
    JUMP_ZERO(0x4C),
    AND(0x4D),
    YIELD(0x4E),
    FORMAT(0x50),
    INC(0x51),
    DEC(0x52),
    COPY_TOP(0x53),
    RETURN_FALSE(0x54),
    RETURN_TRUE(0x55);

    companion object {
        private val VALUES = values().associate { it.opcode to it }

        fun findByValue(value: Byte): Opcode3DS {
            return VALUES[value] ?: error("Unknown opcode: $value.")
        }
    }

    fun toTokenType(): TokenType {
        return when (this) {
            FIX -> TokenType.FIX
            FLOAT -> TokenType.FLOAT
            PLUS -> TokenType.PLUS
            FPLUS -> TokenType.FPLUS
            MINUS -> TokenType.MINUS
            FMINUS -> TokenType.FMINUS
            TIMES -> TokenType.TIMES
            FTIMES -> TokenType.FTIMES
            DIVIDE -> TokenType.DIVIDE
            FDIVIDE -> TokenType.FDIVIDE
            MODULO -> TokenType.MODULO
            NEGATE_INT -> TokenType.MINUS
            NEGATE_REAL -> TokenType.FMINUS
            BNOT -> TokenType.BNOT
            NOT -> TokenType.NOT
            BOR -> TokenType.BOR
            BAND -> TokenType.BAND
            XOR -> TokenType.XOR
            LS -> TokenType.LS
            RS -> TokenType.RS
            EQ -> TokenType.EQ
            FEQ -> TokenType.FEQ
            NE -> TokenType.NE
            FNE -> TokenType.FNE
            LT -> TokenType.LT
            FLT -> TokenType.FLT
            LE -> TokenType.LE
            FLE -> TokenType.FLE
            GT -> TokenType.GT
            FGT -> TokenType.FGT
            GE -> TokenType.GE
            FGE -> TokenType.FGE
            OR -> TokenType.OR
            AND -> TokenType.AND
            INC -> TokenType.INC
            DEC -> TokenType.DEC
            else -> throw IllegalArgumentException("Given opcode does not have a corresponding token type.")
        }
    }
}

fun TokenType.toOpcode3DS(): Opcode3DS {
    return when(this) {
        TokenType.FIX -> Opcode3DS.FIX
        TokenType.FLOAT -> Opcode3DS.FLOAT
        TokenType.PLUS -> Opcode3DS.PLUS
        TokenType.MINUS -> Opcode3DS.MINUS
        TokenType.TIMES -> Opcode3DS.TIMES
        TokenType.DIVIDE -> Opcode3DS.DIVIDE
        TokenType.MODULO -> Opcode3DS.MODULO
        TokenType.FPLUS -> Opcode3DS.FPLUS
        TokenType.FMINUS -> Opcode3DS.FMINUS
        TokenType.FTIMES -> Opcode3DS.FTIMES
        TokenType.FDIVIDE -> Opcode3DS.FDIVIDE
        TokenType.EQ -> Opcode3DS.EQ
        TokenType.NE -> Opcode3DS.NE
        TokenType.LT -> Opcode3DS.LT
        TokenType.LE -> Opcode3DS.LE
        TokenType.GT -> Opcode3DS.GT
        TokenType.GE -> Opcode3DS.GE
        TokenType.FEQ -> Opcode3DS.FEQ
        TokenType.FNE -> Opcode3DS.FNE
        TokenType.FLT -> Opcode3DS.FLT
        TokenType.FLE -> Opcode3DS.FLE
        TokenType.FGT -> Opcode3DS.FGT
        TokenType.FGE -> Opcode3DS.FGE
        TokenType.RS -> Opcode3DS.RS
        TokenType.LS -> Opcode3DS.LS
        TokenType.BAND -> Opcode3DS.BAND
        TokenType.BOR -> Opcode3DS.BOR
        TokenType.XOR -> Opcode3DS.XOR
        TokenType.BNOT -> Opcode3DS.BNOT
        TokenType.NOT -> Opcode3DS.NOT
        TokenType.AND -> Opcode3DS.AND
        TokenType.OR -> Opcode3DS.OR
        TokenType.INC -> Opcode3DS.INC
        TokenType.DEC -> Opcode3DS.DEC
        TokenType.ASSIGN_PLUS -> Opcode3DS.PLUS
        TokenType.ASSIGN_MINUS -> Opcode3DS.MINUS
        TokenType.ASSIGN_TIMES -> Opcode3DS.TIMES
        TokenType.ASSIGN_DIVIDE -> Opcode3DS.DIVIDE
        TokenType.ASSIGN_MODULO -> Opcode3DS.MODULO
        TokenType.ASSIGN_FPLUS -> Opcode3DS.FPLUS
        TokenType.ASSIGN_FMINUS -> Opcode3DS.FMINUS
        TokenType.ASSIGN_FTIMES -> Opcode3DS.FTIMES
        TokenType.ASSIGN_FDIVIDE -> Opcode3DS.FDIVIDE
        else -> throw IllegalArgumentException("$this does not have a corresponding opcode.")
    }
}