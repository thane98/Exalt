package com.thane98.exalt.interfaces

import com.thane98.exalt.compiler.Parser
import com.thane98.exalt.model.expr.Expr

interface InfixParselet {
    fun parse(lhs: Expr, parser: Parser): Expr
    fun precedence(): Int
}