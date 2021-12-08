package com.thane98.exalt.model

import com.thane98.exalt.interfaces.Opcode
import com.thane98.exalt.model.expr.Increment
import com.thane98.exalt.model.opcode.*

enum class Operator {
    ADD,
    FLOAT_ADD,
    SUBTRACT,
    FLOAT_SUBTRACT,
    MULTIPLY,
    FLOAT_MULTIPLY,
    DIVIDE,
    FLOAT_DIVIDE,
    MODULO,
    NEGATE,
    FLOAT_NEGATE,
    BINARY_NOT,
    LOGICAL_NOT,
    BINARY_OR,
    BINARY_AND,
    XOR,
    LEFT_SHIFT,
    RIGHT_SHIFT,
    LOGICAL_AND,
    LOGICAL_OR,
    EQUAL,
    FLOAT_EQUAL,
    NOT_EQUAL,
    FLOAT_NOT_EQUAL,
    LESS_THAN,
    FLOAT_LESS_THAN,
    LESS_THAN_OR_EQUAL_TO,
    FLOAT_LESS_THAN_OR_EQUAL_TO,
    GREATER_THAN,
    FLOAT_GREATER_THAN,
    GREATER_THAN_OR_EQUAL_TO,
    FLOAT_GREATER_THAN_OR_EQUAL_TO,
    INCREMENT,
    DECREMENT,
    ASSIGN,
    ASSIGN_ADD,
    ASSIGN_FLOAT_ADD,
    ASSIGN_SUBTRACT,
    ASSIGN_FLOAT_SUBTRACT,
    ASSIGN_MULTIPLY,
    ASSIGN_FLOAT_MULTIPLY,
    ASSIGN_DIVIDE,
    ASSIGN_FLOAT_DIVIDE,
    ASSIGN_MODULO,
    ASSIGN_BINARY_OR,
    ASSIGN_BINARY_AND,
    ASSIGN_XOR,
    ASSIGN_LEFT_SHIFT,
    ASSIGN_RIGHT_SHIFT;

    companion object {
        const val NO_PRECEDENCE = -1
        const val PREFIX_PRECEDENCE = 11
        const val POSTFIX_PRECEDENCE = 12
    }

    fun precedence(): Int {
        return when (this) {
            ADD -> 9
            FLOAT_ADD -> 9
            SUBTRACT -> 9
            FLOAT_SUBTRACT -> 9
            MULTIPLY -> 10
            FLOAT_MULTIPLY -> 10
            DIVIDE -> 10
            FLOAT_DIVIDE -> 10
            MODULO -> 10
            NEGATE -> 11
            FLOAT_NEGATE -> 11
            BINARY_NOT -> 11
            LOGICAL_NOT -> 11
            BINARY_OR -> 3
            BINARY_AND -> 5
            XOR -> 4
            LEFT_SHIFT -> 8
            RIGHT_SHIFT -> 8
            LOGICAL_AND -> 2
            LOGICAL_OR -> 1
            EQUAL -> 6
            FLOAT_EQUAL -> 6
            NOT_EQUAL -> 6
            FLOAT_NOT_EQUAL -> 6
            LESS_THAN -> 7
            FLOAT_LESS_THAN -> 7
            LESS_THAN_OR_EQUAL_TO -> 7
            FLOAT_LESS_THAN_OR_EQUAL_TO -> 7
            GREATER_THAN -> 7
            FLOAT_GREATER_THAN -> 7
            GREATER_THAN_OR_EQUAL_TO -> 7
            FLOAT_GREATER_THAN_OR_EQUAL_TO -> 7
            INCREMENT -> 11
            DECREMENT -> 11
            ASSIGN -> 0
            ASSIGN_ADD -> 0
            ASSIGN_FLOAT_ADD -> 0
            ASSIGN_SUBTRACT -> 0
            ASSIGN_FLOAT_SUBTRACT -> 0
            ASSIGN_MULTIPLY -> 0
            ASSIGN_FLOAT_MULTIPLY -> 0
            ASSIGN_DIVIDE -> 0
            ASSIGN_FLOAT_DIVIDE -> 0
            ASSIGN_MODULO -> 0
            ASSIGN_BINARY_OR -> 0
            ASSIGN_BINARY_AND -> 0
            ASSIGN_XOR -> 0
            ASSIGN_LEFT_SHIFT -> 0
            ASSIGN_RIGHT_SHIFT -> 0
        }
    }

    fun toShorthand(): Operator {
        return when (this) {
            ADD -> ASSIGN_ADD
            FLOAT_ADD -> ASSIGN_FLOAT_ADD
            SUBTRACT -> ASSIGN_SUBTRACT
            FLOAT_SUBTRACT -> ASSIGN_FLOAT_SUBTRACT
            MULTIPLY -> ASSIGN_MULTIPLY
            FLOAT_MULTIPLY -> ASSIGN_FLOAT_MULTIPLY
            DIVIDE -> ASSIGN_DIVIDE
            FLOAT_DIVIDE -> ASSIGN_FLOAT_DIVIDE
            MODULO -> ASSIGN_MODULO
            BINARY_OR -> ASSIGN_BINARY_OR
            BINARY_AND -> ASSIGN_BINARY_AND
            XOR -> ASSIGN_XOR
            LEFT_SHIFT -> ASSIGN_LEFT_SHIFT
            RIGHT_SHIFT -> ASSIGN_RIGHT_SHIFT
            ASSIGN_ADD -> this
            ASSIGN_FLOAT_ADD -> this
            ASSIGN_SUBTRACT -> this
            ASSIGN_FLOAT_SUBTRACT -> this
            ASSIGN_MULTIPLY -> this
            ASSIGN_FLOAT_MULTIPLY -> this
            ASSIGN_DIVIDE -> this
            ASSIGN_FLOAT_DIVIDE -> this
            ASSIGN_MODULO -> this
            ASSIGN_BINARY_OR -> this
            ASSIGN_BINARY_AND -> this
            ASSIGN_XOR -> this
            ASSIGN_LEFT_SHIFT -> this
            ASSIGN_RIGHT_SHIFT -> this
            else -> throw UnsupportedOperationException("Operator does not have a shorthand equivalent.")
        }
    }

    fun expandShorthand(): Operator {
        return when (this) {
            ASSIGN_ADD -> ADD
            ASSIGN_FLOAT_ADD -> FLOAT_ADD
            ASSIGN_SUBTRACT -> SUBTRACT
            ASSIGN_FLOAT_SUBTRACT -> FLOAT_SUBTRACT
            ASSIGN_MULTIPLY -> MULTIPLY
            ASSIGN_FLOAT_MULTIPLY -> FLOAT_MULTIPLY
            ASSIGN_DIVIDE -> DIVIDE
            ASSIGN_FLOAT_DIVIDE -> FLOAT_DIVIDE
            ASSIGN_MODULO -> MODULO
            ASSIGN_BINARY_OR -> BINARY_OR
            ASSIGN_BINARY_AND -> BINARY_AND
            ASSIGN_XOR -> XOR
            ASSIGN_LEFT_SHIFT -> LEFT_SHIFT
            ASSIGN_RIGHT_SHIFT -> RIGHT_SHIFT
            else -> throw UnsupportedOperationException("Operator '$this' is not in shorthand form.")
        }
    }

    fun toOpcode(label: String? = null): Opcode {
        return when(this) {
            ADD -> Add()
            FLOAT_ADD -> FloatAdd()
            SUBTRACT -> Subtract()
            FLOAT_SUBTRACT -> FloatSubtract()
            MULTIPLY -> Multiply()
            FLOAT_MULTIPLY -> FloatMultiply()
            DIVIDE -> Divide()
            FLOAT_DIVIDE -> FloatDivide()
            MODULO -> Modulo()
            NEGATE -> Negate()
            FLOAT_NEGATE -> FloatNegate()
            BINARY_NOT -> BinaryNot()
            LOGICAL_NOT -> LogicalNot()
            BINARY_OR -> BinaryOr()
            BINARY_AND -> BinaryAnd()
            XOR -> Xor()
            LEFT_SHIFT -> LeftShift()
            RIGHT_SHIFT -> RightShift()
            LOGICAL_AND -> LogicalAnd(label!!)
            LOGICAL_OR -> LogicalOr(label!!)
            EQUAL -> Equal()
            FLOAT_EQUAL -> FloatEqual()
            NOT_EQUAL -> NotEqual()
            FLOAT_NOT_EQUAL -> FloatNotEqual()
            LESS_THAN -> LessThan()
            FLOAT_LESS_THAN -> FloatLessThan()
            LESS_THAN_OR_EQUAL_TO -> LessThanOrEqualTo()
            FLOAT_LESS_THAN_OR_EQUAL_TO -> FloatLessThanOrEqualTo()
            GREATER_THAN -> GreaterThan()
            FLOAT_GREATER_THAN -> GreaterThanOrEqualTo()
            GREATER_THAN_OR_EQUAL_TO -> GreaterThanOrEqualTo()
            FLOAT_GREATER_THAN_OR_EQUAL_TO -> FloatGreaterThanOrEqualTo()
            INCREMENT -> Inc()
            DECREMENT -> Dec()
            ASSIGN -> Assign()
            else -> throw UnsupportedOperationException("Operator '$this' does not correspond to an opcode.")
        }
    }

    override fun toString(): String {
        return when (this) {
            ADD -> "+"
            FLOAT_ADD -> "+f"
            SUBTRACT -> "-"
            FLOAT_SUBTRACT -> "-f"
            MULTIPLY -> "*"
            FLOAT_MULTIPLY -> "*f"
            DIVIDE -> "/"
            FLOAT_DIVIDE -> "/f"
            MODULO -> "%"
            NEGATE -> "-"
            FLOAT_NEGATE -> "-f"
            BINARY_NOT -> "~"
            LOGICAL_NOT -> "!"
            BINARY_OR -> "|"
            BINARY_AND -> "&"
            XOR -> "^"
            LEFT_SHIFT -> "<<"
            RIGHT_SHIFT -> ">>"
            LOGICAL_AND -> "&&"
            LOGICAL_OR -> "||"
            EQUAL -> "=="
            FLOAT_EQUAL -> "==f"
            NOT_EQUAL -> "!="
            FLOAT_NOT_EQUAL -> "!=f"
            LESS_THAN -> "<"
            FLOAT_LESS_THAN -> "<f"
            LESS_THAN_OR_EQUAL_TO -> "<="
            FLOAT_LESS_THAN_OR_EQUAL_TO -> "<=f"
            GREATER_THAN -> ">"
            FLOAT_GREATER_THAN -> ">f"
            GREATER_THAN_OR_EQUAL_TO -> ">="
            FLOAT_GREATER_THAN_OR_EQUAL_TO -> ">=f"
            INCREMENT -> "++"
            DECREMENT -> "--"
            ASSIGN -> "="
            ASSIGN_ADD -> "+="
            ASSIGN_FLOAT_ADD -> "+=f"
            ASSIGN_SUBTRACT -> "-="
            ASSIGN_FLOAT_SUBTRACT -> "-=f"
            ASSIGN_MULTIPLY -> "*="
            ASSIGN_FLOAT_MULTIPLY -> "*=f"
            ASSIGN_DIVIDE -> "/="
            ASSIGN_FLOAT_DIVIDE -> "/=f"
            ASSIGN_MODULO -> "%="
            ASSIGN_BINARY_OR -> "|="
            ASSIGN_BINARY_AND -> "&="
            ASSIGN_XOR -> "^="
            ASSIGN_LEFT_SHIFT -> "<<="
            ASSIGN_RIGHT_SHIFT -> ">>="
        }
    }
}