package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.DeclVisitor
import com.thane98.exalt.interfaces.ExprVisitor
import com.thane98.exalt.interfaces.StmtVisitor
import com.thane98.exalt.model.Script
import com.thane98.exalt.model.decl.Decl
import com.thane98.exalt.model.decl.EventDecl
import com.thane98.exalt.model.decl.FunctionDecl
import com.thane98.exalt.model.decl.ScriptDecl
import com.thane98.exalt.model.expr.*
import com.thane98.exalt.model.stmt.*
import com.thane98.exalt.model.symbol.VarSymbol
import kotlin.math.max

class FrameDataCalculator private constructor() : DeclVisitor<Unit>, StmtVisitor<Unit>, ExprVisitor<Unit> {
    private var nextFrameId = 0
    private var frameSize = 0

    companion object {
        fun calculate(script: Script) {
            val calculator = FrameDataCalculator()
            script.contents.forEach { it.accept(calculator) }
        }

        fun calculate(decl: Decl): Int {
            val calculator = FrameDataCalculator()
            decl.accept(calculator)
            return calculator.frameSize
        }
    }

    private fun resetForNextFunction() {
        nextFrameId = 0
        frameSize = 0
    }

    private fun setSymbolFrameId(symbol: VarSymbol): Int {
        val frameId = if (symbol.frameId != null) {
            symbol.frameId!!
        } else {
            nextFrameId++
        }
        symbol.frameId = frameId
        return frameId
    }

    override fun visitScriptDecl(decl: ScriptDecl) {}

    override fun visitEventDecl(decl: EventDecl) {
        resetForNextFunction()
        decl.body.accept(this)
        decl.frameSize = frameSize
    }

    override fun visitFunctionDecl(decl: FunctionDecl) {
        resetForNextFunction()
        for (symbol in decl.parameters) {
            val frameId = setSymbolFrameId(symbol)
            frameSize = max(frameId + 1, frameSize)
        }
        decl.body.accept(this)
        decl.frameSize = frameSize
    }

    override fun visitLiteral(expr: Literal) {}

    override fun visitGrouped(expr: Grouped) {
        expr.inner.accept(this)
    }

    override fun visitVarRef(expr: VarRef) {
        val frameId = setSymbolFrameId(expr.symbol)
        frameSize = max(frameId + 1, frameSize)
    }

    override fun visitArrayRef(expr: ArrayRef) {
        val frameId = setSymbolFrameId(expr.symbol)
        expr.symbol.frameId = frameId
        frameSize = if (expr.index is Literal && expr.index.intValueOrNull() != null && expr.index.intValue() > 0) {
            val maxFrameId = frameId + expr.index.intValue()
            max(maxFrameId + 1, frameId)
        } else {
            max(frameId + 1, frameSize)
        }
    }

    override fun visitPointerRef(expr: PointerRef) {
        val frameId = setSymbolFrameId(expr.symbol)
        expr.symbol.frameId = frameId
        frameSize = if (expr.index is Literal && expr.index.intValueOrNull() != null && expr.index.intValue() > 0) {
            val maxFrameId = frameId + expr.index.intValue()
            max(maxFrameId + 1, frameId)
        } else {
            max(frameId + 1, frameSize)
        }
    }

    override fun visitUnary(expr: Unary) {
        expr.operand.accept(this)
    }

    override fun visitBinary(expr: Binary) {
        expr.left.accept(this)
        expr.right.accept(this)
    }

    override fun visitFuncall(expr: Funcall) {
        expr.args.forEach { it.accept(this) }
    }

    override fun visitAssignment(expr: Assignment) {
        expr.left.accept(this)
        expr.right.accept(this)
    }

    override fun visitIncrement(expr: Increment) {
        expr.operand.accept(this)
    }

    override fun visitBlock(stmt: Block) {
        stmt.contents.forEach { it.accept(this) }
    }

    override fun visitExprStmt(stmt: ExprStmt) {
        stmt.expr.accept(this)
    }

    override fun visitFor(stmt: For) {
        stmt.init.accept(this)
        stmt.check.accept(this)
        stmt.step.accept(this)
        stmt.body.accept(this)
    }

    override fun visitGoto(stmt: Goto) {}

    override fun visitIf(stmt: If) {
        stmt.condition.accept(this)
        stmt.thenPart.accept(this)
        stmt.elsePart?.accept(this)
    }

    override fun visitLabel(stmt: Label) {}

    override fun visitMatch(stmt: Match) {
        stmt.switch.accept(this)
        stmt.cases.forEach {
            it.condition.accept(this)
            it.body.accept(this)
        }
        stmt.default?.accept(this)
    }

    override fun visitReturn(stmt: Return) {
        stmt.value.accept(this)
    }

    override fun visitWhile(stmt: While) {
        stmt.condition.accept(this)
        stmt.body.accept(this)
    }

    override fun visitYield(stmt: Yield) {}
}