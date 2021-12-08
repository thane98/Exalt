package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.DeclVisitor
import com.thane98.exalt.interfaces.ExprVisitor
import com.thane98.exalt.interfaces.StmtVisitor
import com.thane98.exalt.model.LiteralType
import com.thane98.exalt.model.Script
import com.thane98.exalt.model.decl.EventDecl
import com.thane98.exalt.model.decl.FunctionDecl
import com.thane98.exalt.model.decl.ScriptDecl
import com.thane98.exalt.model.expr.*
import com.thane98.exalt.model.stmt.*

// This name is a little misleading. The purpose of this script is to identify NAMED
// functions which aren't referenced in an event's arguments. This is needed to know
// whether to write the function name to text data.
//
// As a side note, not sure if this functionality is actually needed for functionality.
// It's implemented to get accurate (identical) output compared to the base game.
class UnreferencedFunctionFinder private constructor(
    private val functions: Set<String>,
    private val referencedFunctions: MutableSet<String> = mutableSetOf(),
) : DeclVisitor<Unit>, StmtVisitor<Unit>, ExprVisitor<Unit> {
    companion object {
        fun findUnreferencedFunctions(script: Script, isV3: Boolean): Set<String> {
            val namedFunctions = NamedFunctionFinder.findAllNamedFunctions(script, isV3)
            val finder = UnreferencedFunctionFinder(namedFunctions)
            script.contents.forEach { it.accept(finder) }
            return namedFunctions - finder.referencedFunctions
        }
    }

    override fun visitScriptDecl(decl: ScriptDecl) {}

    override fun visitEventDecl(decl: EventDecl) {
        decl.args.forEach { it.accept(this) }
        decl.body.accept(this)
    }

    override fun visitFunctionDecl(decl: FunctionDecl) {
        decl.body.accept(this)
    }

    override fun visitLiteral(expr: Literal) {
        if (expr.literalType() == LiteralType.STR && expr.stringValue() in functions) {
            referencedFunctions.add(expr.stringValue())
        }
    }

    override fun visitGrouped(expr: Grouped) {
        expr.inner.accept(this)
    }

    override fun visitVarRef(expr: VarRef) {}

    override fun visitArrayRef(expr: ArrayRef) {}

    override fun visitPointerRef(expr: PointerRef) {}

    override fun visitUnary(expr: Unary) {
        expr.operand.accept(this)
    }

    override fun visitBinary(expr: Binary) {
        expr.left.accept(this)
        expr.right.accept(this)
    }

    override fun visitFuncall(expr: Funcall) {
        if (expr.symbol.name in functions) {
            referencedFunctions.add(expr.symbol.name)
        }
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