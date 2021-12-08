package com.thane98.exalt.compiler.parselet

import com.thane98.exalt.compiler.Parser
import com.thane98.exalt.error.CompilerError
import com.thane98.exalt.interfaces.InfixParselet
import com.thane98.exalt.model.Operator
import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.expr.Increment
import com.thane98.exalt.model.expr.Ref

class PostfixIncrementParselet(private val op: Operator) : InfixParselet {
    override fun precedence(): Int {
        return Operator.POSTFIX_PRECEDENCE
    }

    override fun parse(lhs: Expr, parser: Parser): Expr {
        if (lhs !is Ref)
            throw CompilerError.at(parser.previous.position, "Expected reference.")
        return Increment(op, lhs, false)
    }
}