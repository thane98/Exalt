package com.thane98.exalt.model.expr

import com.thane98.exalt.error.LiteralTypeException
import com.thane98.exalt.interfaces.ExprVisitor
import com.thane98.exalt.model.LiteralType

data class Literal(private var storedType: LiteralType, private var value: Any) : Expr {
    companion object {
        fun ofInt(value: Int): Literal {
            return Literal(LiteralType.INT, value)
        }

        fun ofFloat(value: Float): Literal {
            return Literal(LiteralType.FLOAT, value)
        }

        fun ofString(value: String): Literal {
            return Literal(LiteralType.STR, value)
        }
    }

    init {
        if (storedType == LiteralType.INT && value !is Int) {
            throw LiteralTypeException(storedType)
        } else if (storedType == LiteralType.FLOAT && value !is Float) {
            throw LiteralTypeException(storedType)
        } else if (storedType == LiteralType.STR && value !is String) {
            throw LiteralTypeException(storedType)
        }
    }

    override fun <T> accept(visitor: ExprVisitor<T>): T {
        return visitor.visitLiteral(this)
    }

    fun literalType(): LiteralType {
        return storedType
    }

    fun intValueOrNull(): Int? {
        return if (storedType != LiteralType.INT) {
            null
        } else {
            this.value as Int
        }
    }

    fun intValue(): Int {
        if (storedType != LiteralType.INT) {
            throw LiteralTypeException(LiteralType.INT)
        } else {
            return this.value as Int
        }
    }

    fun floatValueOrNull(): Float? {
        return if (storedType != LiteralType.FLOAT) {
            null
        } else {
            this.value as Float
        }
    }

    fun floatValue(): Float {
        if (storedType != LiteralType.FLOAT) {
            throw LiteralTypeException(LiteralType.FLOAT)
        } else {
            return this.value as Float
        }
    }

    fun stringValueOrNull(): String? {
        return if (storedType != LiteralType.STR) {
            null
        } else {
            this.value as String
        }
    }

    fun stringValue(): String {
        if (storedType != LiteralType.STR) {
            throw LiteralTypeException(LiteralType.STR)
        } else {
            return this.value as String
        }
    }

    override fun toString(): String {
        return when (storedType) {
            LiteralType.INT -> value.toString()
            LiteralType.FLOAT -> (value as Float).toBigDecimal().toPlainString()
            LiteralType.STR -> "\"$value\""
        }
    }
}
