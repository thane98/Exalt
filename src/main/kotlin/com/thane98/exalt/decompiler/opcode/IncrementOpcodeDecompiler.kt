package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.Operator
import com.thane98.exalt.model.expr.Funcall
import com.thane98.exalt.model.expr.Increment
import com.thane98.exalt.model.expr.Ref

class IncrementOpcodeDecompiler(
    private val op: Operator
) : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        val operand = popOperand(state)
        val prefix = !isPostfix(state, operand)
        if (prefix) {
            val next = state.codeDecompiler.decompileNext(state)
            if (next != DecompileStepResult.EXPR || state.expressions.peek() !is Ref) {
                throw IllegalStateException("Expected prefix increment to be followed by a reference to the operand.")
            }
        }

        // Increment leaves either the original value (postfix) or new value (prefix) on the stack.
        // This isn't needed for decompiling, so discard it.
        state.expressions.pop()

        state.expressions.push(Increment(op, operand, prefix))
        return DecompileStepResult.EXPR
    }

    private fun isPostfix(state: CodeDecompilerState, operand: Ref): Boolean {
        if (state.expressions.isEmpty()) {
            return false
        }

        val top = state.expressions.peek()
        return if (top is Ref) {
            top.symbol == operand.symbol
        } else {
            false
        }
    }

    // TODO: This is duplicate code (see AssignmentOpcodeDecompiler)
    private fun popOperand(state: CodeDecompilerState): Ref {
        val left = state.expressions.pop()
        if (left is Ref) {
            return left
        } else if (left is Funcall) {
            if (left.symbol.name == "addr") {
                return left.args.first() as Ref
            }
        }
        throw IllegalStateException("Left hand side of assignment must be a reference.")
    }
}