package com.thane98.exalt.decompiler

import com.thane98.exalt.ast.*

class GotoResolver(private val gotos: Map<Int, List<Goto>>) : StmtVisitor<Unit> {
    private var nextLabel = 0

    override fun visitBlock(stmt: Block) {
        val contents = stmt.contents
        var index = 0
        while (index < contents.size) {
            val line = contents[index]
            if (line.address in gotos) {
                val label = Label(LabelSymbol("L$nextLabel"))
                label.address = line.address
                label.symbol.address = line.address
                nextLabel++
                contents.add(index, label)
                for (goto in gotos[line.address] ?: error("Unexpected state in GotoResolver"))
                    goto.target = label.symbol
                index++ // Skip the label
            }
            line.accept(this)
            index++
        }
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
        for (entry in stmt.cases)
            entry.body.accept(this)
        stmt.default?.accept(this)
    }

}