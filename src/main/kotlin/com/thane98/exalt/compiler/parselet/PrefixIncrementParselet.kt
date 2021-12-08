package com.thane98.exalt.compiler.parselet

import com.thane98.exalt.compiler.Parser
import com.thane98.exalt.error.CompilerError
import com.thane98.exalt.interfaces.PrefixParselet
import com.thane98.exalt.model.Operator
import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.expr.Increment
import com.thane98.exalt.model.expr.Ref

class PrefixIncrementParselet(private val op: Operator) : PrefixParselet {
    override fun parse(parser: Parser): Expr {
        val expr = parser.parseExpr(op.precedence()) as? Ref
            ?: throw CompilerError.at(parser.previous.position, "Expected reference.")
        return Increment(op, expr, true)
    }
}