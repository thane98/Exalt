package com.thane98.exalt.compiler.parselet

import com.thane98.exalt.compiler.Parser
import com.thane98.exalt.error.CompilerError
import com.thane98.exalt.interfaces.InfixParselet
import com.thane98.exalt.model.Operator
import com.thane98.exalt.model.expr.Assignment
import com.thane98.exalt.model.expr.Binary
import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.expr.Ref

class AssignmentParselet(private val op: Operator) : InfixParselet {
    override fun parse(lhs: Expr, parser: Parser): Expr {
        if (lhs !is Ref)
            throw CompilerError.at(
                parser.previous.position,
                "Left hand side of assignment must be a reference.",
            )
        return Assignment(lhs, parser.parseExpr(op.precedence()), op)
    }

    override fun precedence(): Int {
        return op.precedence()
    }
}