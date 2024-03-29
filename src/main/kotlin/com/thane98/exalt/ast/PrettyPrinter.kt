package com.thane98.exalt.ast

import com.thane98.exalt.common.TokenType

class PrettyPrinter private constructor(): ExprVisitor<Unit>, StmtVisitor<Unit> {
    private val sb = StringBuilder()
    private var indentLevel = 0

    companion object {
        fun print(script: Block): String {
            val printer = PrettyPrinter()
            for (entry in script.contents) {
                entry.accept(printer)
                if (entry != script.contents.last())
                    printer.sb.appendLine()
            }
            return printer.sb.toString()
        }
    }

    private fun indent() {
        for (i in 0 until (indentLevel * 4))
            sb.append(' ')
    }

    override fun visitLiteral(expr: Literal) {
        when {
            expr.value is String -> sb.append('"').append(expr.value).append('"')
            expr.value is Int -> sb.append(expr.value)
            expr.value is Float -> {
                sb.append(expr.value.toBigDecimal().toPlainString())
            }
            else -> assert(false) { "Unexpected value in literal." }
        }
    }

    override fun visitUnaryExpr(expr: UnaryExpr) {
        if (expr.op == TokenType.FIX || expr.op == TokenType.FLOAT) {
            sb.append(expr.op.toString()).append('(')
            expr.expr.accept(this)
            sb.append(')')
        } else {
            sb.append(expr.op.toString())
            expr.expr.accept(this)
        }
    }

    override fun visitIncrement(expr: Increment) {
        if (expr.isPrefix) {
            sb.append(expr.op.toString())
            expr.target.accept(this)
        } else {
            expr.target.accept(this)
            sb.append(expr.op.toString())
        }
    }

    override fun visitBinaryExpr(expr: BinaryExpr) {
        // TODO: Move the grouping stuff somewhere else.
        //       Doing it in the PrettyPrinter is incorrect.
        if (needsGrouping(expr.op, expr.lhs))
            printWithGrouping(expr.lhs)
        else
            expr.lhs.accept(this)
        sb.append(' ').append(expr.op.toString()).append(' ')
        if (needsGrouping(expr.op, expr.rhs))
            printWithGrouping(expr.rhs)
        else
            expr.rhs.accept(this)
    }

    private fun needsGrouping(op: TokenType, expr: Expr): Boolean {
        if (expr is BinaryExpr)
            return expr.op.precedence() < op.precedence()
        return false
    }

    private fun printWithGrouping(expr: Expr) {
        sb.append('(')
        expr.accept(this)
        sb.append(')')
    }

    override fun visitGroupedExpr(expr: GroupedExpr) {
        printWithGrouping(expr.expr)
    }

    override fun visitFuncall(expr: Funcall) {
        sb.append(expr.target).append('(')
        for (arg in expr.args) {
            arg.accept(this)
            if (arg != expr.args.last())
                sb.append(", ")
        }
        sb.append(')')
    }

    override fun visitVarRef(expr: VarRef) {
        printRef(expr.isPointer, expr.symbol)
    }

    override fun visitArrayRef(expr: ArrayRef) {
        printRef(expr.isPointer, expr.symbol)
        sb.append('[')
        expr.index.accept(this)
        sb.append(']')
    }

    private fun printRef(isPointer: Boolean, symbol: VarSymbol) {
        if (isPointer)
            sb.append('&')
        if (symbol.name.isEmpty())
            sb.append('$').append(symbol.frameID)
        else
            sb.append(symbol.name)
    }

    override fun visitBlock(stmt: Block) {
        if (stmt.contents.isEmpty())
            sb.appendLine("{}")
        else {
            sb.appendLine('{')
            indentLevel++
            for (line in stmt.contents) {
                indent()
                line.accept(this)
            }
            indentLevel--
            indent()
            sb.appendLine('}')
        }
    }

    override fun visitFuncDecl(stmt: FuncDecl) {
        sb.append("func ").append(stmt.symbol.name).append('(')
        for (sym in stmt.params) {
            if (sym.isExternal)
                sb.append('&')
            sb.append(sym.name)
            if (sym != stmt.params.last())
                sb.append(", ")
        }
        sb.append(") ")
        stmt.contents.accept(this)
    }

    override fun visitEventDecl(stmt: EventDecl) {
        sb.append("event [").append(stmt.symbol.type).append("](")
        for (arg in stmt.args) {
            arg.accept(this)
            if (arg != stmt.args.last())
                sb.append(", ")
        }
        sb.append(") ")
        stmt.contents.accept(this)
    }

    override fun visitIf(stmt: If) {
        sb.append("if(")
        stmt.cond.accept(this)
        sb.append(") ")
        stmt.thenPart.accept(this)
        if (stmt.elsePart != null) {
            indent()
            sb.append("else ")
            stmt.elsePart.accept(this)
        }
    }

    override fun visitLabel(stmt: Label) {
        sb.append("label ").append(stmt.symbol.name).appendLine(';')
    }

    override fun visitGoto(stmt: Goto) {
        assert(stmt.target != null)
        sb.append("goto ").append(stmt.target!!.name).appendLine(';')
    }

    override fun visitExprStmt(stmt: ExprStmt) {
        stmt.expr.accept(this)
        sb.appendLine(';')
    }

    override fun visitReturn(stmt: Return) {
        sb.append("return")
        if (stmt.value != null) {
            sb.append(' ')
            stmt.value.accept(this)
        }
        sb.appendLine(';')
    }

    override fun visitYield(stmt: Yield) {
        sb.appendLine("yield();")
    }

    override fun visitWhile(stmt: While) {
        sb.append("while(")
        stmt.cond.accept(this)
        sb.append(") ")
        stmt.body.accept(this)
    }

    override fun visitFor(stmt: For) {
        sb.append("for(")
        if (stmt.init != null) {
            assert(stmt.init is ExprStmt)
            (stmt.init as ExprStmt).expr.accept(this)
        }
        sb.append("; ")
        stmt.check.accept(this)
        sb.append("; ")
        if (stmt.step != null) {
            assert(stmt.step is ExprStmt)
            (stmt.step as ExprStmt).expr.accept(this)
        }
        sb.append(") ")
        stmt.body.accept(this)
    }

    override fun visitMatch(stmt: Match) {
        sb.append("match(")
        stmt.switch.accept(this)
        sb.appendLine(") {")
        indentLevel++
        for (case in stmt.cases) {
            indent()
            case.cond.accept(this)
            sb.append(" -> ")
            case.body.accept(this)
        }
        if (stmt.default != null) {
            indent()
            sb.append("else -> ")
            stmt.default.accept(this)
        }
        indentLevel--
        indent()
        sb.appendLine('}')
    }
}