package com.thane98.exalt.compiler.parselet

import com.thane98.exalt.compiler.Parser
import com.thane98.exalt.interfaces.PrefixParselet
import com.thane98.exalt.model.TokenType
import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.expr.Grouped

class GroupedParselet : PrefixParselet {
    override fun parse(parser: Parser): Expr {
        val expr = parser.parseExpr()
        parser.consume(TokenType.RPAREN)
        return Grouped(expr)
    }
}