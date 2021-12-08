package com.thane98.exalt.compiler.parselet

import com.thane98.exalt.compiler.Parser
import com.thane98.exalt.interfaces.PrefixParselet
import com.thane98.exalt.model.Operator
import com.thane98.exalt.model.TokenType
import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.expr.Literal
import com.thane98.exalt.model.expr.Unary

class MinusParselet(private val op: Operator) : PrefixParselet {
    override fun parse(parser: Parser): Expr {
        return if (parser.match(TokenType.INT) && parser.features.simplifyNegativeInts)
            Literal.ofInt(-(parser.previousValue.intValue()))
        else if (parser.previous.type == TokenType.INT)
            Unary(op, parser.previousValue)
        else
            Unary(op, parser.parseExpr(op.precedence()))
    }
}