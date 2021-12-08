package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.AssignmentState
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.Operator
import com.thane98.exalt.model.expr.*
import com.thane98.exalt.model.stmt.ExprStmt
import kotlin.reflect.typeOf

class AssignmentOpcodeDecompiler : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        val op: Operator
        val left: Expr
        val right: Expr
        if (state.assignmentState == AssignmentState.NORMAL) {
            op = Operator.ASSIGN
            right = state.expressions.pop()
            left = unwrapAddr(state.expressions.pop())
        } else {
            val expr = state.expressions.pop()
            if (expr !is Binary) {
                throw IllegalStateException("Shorthand assignment only applies to binary operations.")
            }
            op = expr.op.toShorthand()
            right = expr.right
            left = unwrapAddr(expr.left)
        }

        state.blocks.line(ExprStmt(Assignment(left, right, op)))
        state.assignmentState = AssignmentState.NORMAL
        return DecompileStepResult.STMT
    }

    private fun unwrapAddr(expr: Expr): Ref {
        if (expr is Ref) {
            return expr
        } else if (expr is Funcall) {
            if (expr.symbol.name == "addr") {
                return expr.args.first() as Ref
            }
        }
        throw IllegalStateException("Left hand side of assignment must be a reference.")
    }
}