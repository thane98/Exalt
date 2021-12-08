package com.thane98.exalt.compiler.parselet

import com.thane98.exalt.compiler.Parser
import com.thane98.exalt.interfaces.PrefixParselet
import com.thane98.exalt.model.expr.Expr

class LiteralParselet : PrefixParselet {
    override fun parse(parser: Parser): Expr {
        return parser.previousValue
    }
}