package com.thane98.exalt.compiler.parselet

import com.thane98.exalt.compiler.Parser
import com.thane98.exalt.interfaces.InfixParselet
import com.thane98.exalt.model.Operator
import com.thane98.exalt.model.expr.Binary
import com.thane98.exalt.model.expr.Expr

class InfixOperatorParselet(private val op: Operator) : InfixParselet {
    override fun precedence(): Int {
        return op.precedence()
    }

    override fun parse(lhs: Expr, parser: Parser): Expr {
        return Binary(lhs, parser.parseExpr(precedence()), op)
    }
}