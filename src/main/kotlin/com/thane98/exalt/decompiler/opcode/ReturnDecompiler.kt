package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.LiteralType
import com.thane98.exalt.model.decl.Annotation
import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.expr.Literal
import com.thane98.exalt.model.stmt.Return

class ReturnDecompiler(
    private val builtInValue: Literal? = null
) : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        val value = builtInValue ?: state.expressions.pop()
        if (isLongFormReturn(value)) {
            state.requestedAnnotations.add(Annotation("LongReturn"))
        }
        state.blocks.line(Return(value))
        return DecompileStepResult.STMT
    }

    private fun isLongFormReturn(expr: Expr): Boolean {
        return builtInValue == null
                && expr is Literal
                && expr.literalType() == LiteralType.INT
                && (expr.intValue() == 0 || expr.intValue() == 1)
    }
}