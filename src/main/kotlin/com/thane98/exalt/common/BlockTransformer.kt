package com.thane98.exalt.common

import com.thane98.exalt.interfaces.StmtVisitor
import com.thane98.exalt.model.stmt.*

abstract class BlockTransformer : StmtVisitor<Unit> {
    override fun visitExprStmt(stmt: ExprStmt) {}

    override fun visitFor(stmt: For) {
        stmt.body.accept(this)
    }

    override fun visitGoto(stmt: Goto) {}

    override fun visitIf(stmt: If) {
        stmt.thenPart.accept(this)
        stmt.elsePart?.accept(this)
    }

    override fun visitLabel(stmt: Label) {}

    override fun visitMatch(stmt: Match) {
        for (case in stmt.cases) {
            case.body.accept(this)
        }
        stmt.default?.accept(this)
    }

    override fun visitReturn(stmt: Return) {}

    override fun visitWhile(stmt: While) {
        stmt.body.accept(this)
    }

    override fun visitYield(stmt: Yield) {}
}