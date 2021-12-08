package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.Operator
import com.thane98.exalt.model.expr.Unary

class UnaryOpcodeDecompiler(private val op: Operator) : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        val operand = state.expressions.pop()
        state.expressions.push(Unary(op, operand))
        return DecompileStepResult.EXPR
    }
}