package com.thane98.exalt.compiler.parselet

import com.thane98.exalt.compiler.Parser
import com.thane98.exalt.interfaces.PrefixParselet
import com.thane98.exalt.model.Operator
import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.expr.Unary

class PrefixOperatorParselet(private val op: Operator) : PrefixParselet {
    override fun parse(parser: Parser): Expr {
        return Unary(op, parser.parseExpr(op.precedence()))
    }
}