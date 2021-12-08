package com.thane98.exalt.compiler.parselet

import com.thane98.exalt.compiler.Parser
import com.thane98.exalt.error.CompilerError
import com.thane98.exalt.interfaces.PrefixParselet
import com.thane98.exalt.model.Operator
import com.thane98.exalt.model.expr.ArrayRef
import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.expr.PointerRef
import com.thane98.exalt.model.expr.Ref

class PointerParselet : PrefixParselet {
    override fun parse(parser: Parser): Expr {
        val expr = parser.parseExpr(Operator.PREFIX_PRECEDENCE)
        if (expr !is ArrayRef) {
            throw CompilerError.at(
                parser.previous.position,
                "The pointer operator can only be applied to array references."
            )
        }
        return PointerRef(expr.symbol, expr.index)
    }
}