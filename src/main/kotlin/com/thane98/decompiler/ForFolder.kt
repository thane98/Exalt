package com.thane98.decompiler

import com.thane98.ast.*

class ForFolder: StmtVisitor<Unit> {
    override fun visitBlock(stmt: Block) {
        val contents = stmt.contents
        var index = 0
        while (index < contents.size) {
            if (isForLoopSequence(contents, index)) {
                val init = contents[index]
                val step = contents[index + 3]
                val ifBody = contents[index + 5] as If
                val bodyBlock = ifBody.thenPart as Block
                val check = ifBody.cond

                // Remove old statements
                bodyBlock.contents.removeAt(bodyBlock.contents.lastIndex)
                contents.subList(index, index + 5).clear()
                contents[index] = For(init, check, step, ifBody.thenPart)
            }
            contents[index].accept(this)
            index++
        }
    }

    private fun isForLoopSequence(stmts: List<Stmt>, index: Int): Boolean {
        if (index + 5 >= stmts.size)
            return false
        return stmts[index] is ExprStmt
                && stmts[index + 1] is Goto
                && stmts[index + 2] is Label
                && stmts[index + 3] is ExprStmt
                && stmts[index + 4] is Label
                && stmts[index + 5] is If
    }

    override fun visitFuncDecl(stmt: FuncDecl) {
        stmt.contents.accept(this)
    }

    override fun visitEventDecl(stmt: EventDecl) {
        stmt.contents.accept(this)
    }

    override fun visitIf(stmt: If) {
        stmt.thenPart.accept(this)
        stmt.elsePart?.accept(this)
    }

    override fun visitLabel(stmt: Label) {}

    override fun visitGoto(stmt: Goto) {}

    override fun visitExprStmt(stmt: ExprStmt) {}

    override fun visitReturn(stmt: Return) {}

    override fun visitYield(stmt: Yield) {}

    override fun visitWhile(stmt: While) {
        stmt.body.accept(this)
    }

    override fun visitFor(stmt: For) {
        stmt.body.accept(this)
    }

    override fun visitMatch(stmt: Match) {
        for (case in stmt.cases)
            case.body.accept(this)
        stmt.default?.accept(this)
    }
}