package com.thane98.exalt.common

import com.thane98.exalt.interfaces.DeclVisitor
import com.thane98.exalt.interfaces.ExprVisitor
import com.thane98.exalt.interfaces.StmtVisitor
import com.thane98.exalt.model.Script
import com.thane98.exalt.model.decl.Annotation
import com.thane98.exalt.model.decl.EventDecl
import com.thane98.exalt.model.decl.FunctionDecl
import com.thane98.exalt.model.decl.ScriptDecl
import com.thane98.exalt.model.expr.*
import com.thane98.exalt.model.stmt.*

class AstPrinter private constructor() : DeclVisitor<Unit>, StmtVisitor<Unit>, ExprVisitor<Unit> {
    private val builder = StringBuilder()
    private var indentLevel = 0

    companion object {
        fun print(script: Script): String {
            val visitor = AstPrinter()
            for (decl in script.contents) {
                decl.accept(visitor)
            }
            return visitor.builder.toString()
        }
    }

    private fun indent() {
        for (i in 0 until indentLevel) {
            builder.append("    ")
        }
    }

    private fun printAnnotations(annotations: List<Annotation>) {
        annotations.forEach {
            builder.append('@').append(it.name)
            if (it.args.isNotEmpty()) {
                builder.append('(')
                builder.append(it.args.joinToString(", ") { arg -> '"' + arg + '"' }).appendLine(')')
            } else {
                builder.appendLine()
            }
        }
    }

    override fun visitScriptDecl(decl: ScriptDecl) {
        builder.appendLine("script(\"${decl.game}\", ${decl.scriptType});")
        builder.append(System.lineSeparator())
    }

    override fun visitEventDecl(decl: EventDecl) {
        printAnnotations(decl.annotations)
        builder.append("event [${decl.eventType}](")
        for (i in 0 until decl.args.size) {
            decl.args[i].accept(this)
            if (i < decl.args.lastIndex) {
                builder.append(", ")
            }
        }
        builder.append(") ")
        decl.body.accept(this)
        builder.append(System.lineSeparator())
    }

    override fun visitFunctionDecl(decl: FunctionDecl) {
        printAnnotations(decl.annotations)
        builder.append("func ").append(decl.symbol.name).append("(")
        for (i in 0 until decl.parameters.size) {
            builder.append(decl.parameters[i])
            if (i < decl.parameters.lastIndex) {
                builder.append(", ")
            }
        }
        builder.append(") ")
        decl.body.accept(this)
        builder.append(System.lineSeparator())
    }

    override fun visitLiteral(expr: Literal) {
        builder.append(expr)
    }

    override fun visitGrouped(expr: Grouped) {
        builder.append('(')
        expr.inner.accept(this)
        builder.append(')')
    }

    override fun visitVarRef(expr: VarRef) {
        builder.append(expr.symbol)
    }

    override fun visitArrayRef(expr: ArrayRef) {
        builder.append(expr.symbol).append('[')
        expr.index.accept(this)
        builder.append(']')
    }

    override fun visitPointerRef(expr: PointerRef) {
        builder.append('&').append(expr.symbol).append('[')
        expr.index.accept(this)
        builder.append(']')
    }

    override fun visitUnary(expr: Unary) {
        builder.append(expr.op)
        expr.operand.accept(this)
    }

    override fun visitBinary(expr: Binary) {
        expr.left.accept(this)
        builder.append(' ').append(expr.op).append(' ')
        expr.right.accept(this)
    }

    override fun visitFuncall(expr: Funcall) {
        builder.append(expr.symbol.name).append('(')
        for (i in 0 until expr.args.size) {
            expr.args[i].accept(this)
            if (i < expr.args.lastIndex) {
                builder.append(", ")
            }
        }
        builder.append(')')
    }

    override fun visitIncrement(expr: Increment) {
        if (expr.isPrefix) {
            builder.append(expr.op)
        }
        expr.operand.accept(this)
        if (!expr.isPrefix) {
            builder.append(expr.op)
        }
    }

    override fun visitAssignment(expr: Assignment) {
        expr.left.accept(this)
        builder.append(' ').append(expr.op).append(' ')
        expr.right.accept(this)
    }

    override fun visitBlock(stmt: Block) {
        if (stmt.contents.isEmpty())
            builder.appendLine("{}")
        else {
            builder.appendLine('{')
            indentLevel++
            for (line in stmt.contents) {
                indent()
                line.accept(this)
            }
            indentLevel--
            indent()
            builder.appendLine('}')
        }
    }

    override fun visitExprStmt(stmt: ExprStmt) {
        stmt.expr.accept(this)
        builder.appendLine(';')
    }

    override fun visitFor(stmt: For) {
        builder.append("for (")
        stmt.init.accept(this)
        builder.append("; ")
        stmt.check.accept(this)
        builder.append("; ")
        stmt.step.expr.accept(this)
        builder.append(") ")
        stmt.body.accept(this)
    }

    override fun visitGoto(stmt: Goto) {
        builder.append("goto ").append(stmt.symbol?.name).appendLine(';')
    }

    override fun visitIf(stmt: If) {
        builder.append("if (")
        stmt.condition.accept(this)
        builder.append(") ")
        stmt.thenPart.accept(this)
        if (stmt.elsePart != null) {
            // TODO: Verify that this is correct for Unix systems...
            builder.delete(builder.length - System.lineSeparator().length + 1, builder.length)
            builder.append(" else ")
            stmt.elsePart.accept(this)
        }
    }

    override fun visitLabel(stmt: Label) {
        builder.append("label ").append(stmt.symbol.name).appendLine(';')
    }

    override fun visitMatch(stmt: Match) {
        builder.append("match (")
        stmt.switch.accept(this)
        builder.appendLine(") {")
        indentLevel++
        for(cs in stmt.cases) {
            indent()
            cs.condition.accept(this)
            builder.append(" -> ")
            cs.body.accept(this)
        }
        if (stmt.default != null) {
            indent()
            builder.append("else -> ")
            stmt.default?.accept(this)
        }
        indentLevel--
        indent()
        builder.appendLine('}')
    }

    override fun visitReturn(stmt: Return) {
        builder.append("return ")
        stmt.value.accept(this)
        builder.appendLine(";")
    }

    override fun visitWhile(stmt: While) {
        builder.append("while (")
        stmt.condition.accept(this)
        builder.append(") ")
        stmt.body.accept(this)
    }

    override fun visitYield(stmt: Yield) {
        builder.appendLine("yield;")
    }
}