package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.Operator
import com.thane98.exalt.model.expr.Binary
import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.expr.Grouped

class BinaryOpcodeDecompiler(private val op: Operator) : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        val right = preservePrecedence(state.expressions.pop())
        val left = preservePrecedence(state.expressions.pop())
        state.expressions.push(Binary(left, right, op))
        return DecompileStepResult.EXPR
    }

    private fun preservePrecedence(expr: Expr): Expr {
        return if (expr !is Binary || expr.op.precedence() > op.precedence()) {
            expr
        } else {
            Grouped(expr)
        }
    }
}