package decompiler

import ast.*

class WhileFolder: StmtVisitor<Unit> {
    override fun visitBlock(stmt: Block) {
        var index = 0
        val contents = stmt.contents
        while (index < contents.size - 1) {
            val line = contents[index]
            val nextLine = contents[index + 1]
            if (line is Label && nextLine is If && nextLine.thenPart is Block && nextLine.elsePart == null) {
                val block = nextLine.thenPart.contents
                val goto = block.last() as? Goto
                if (goto != null && goto.target == line.symbol) {
                    // Found a while loop!
                    // First, get rid of the label and goto.
                    val folded = While(nextLine.cond, nextLine.thenPart)
                    folded.accept(this)
                    block.removeAt(block.lastIndex)
                    contents[index] = folded
                    contents.removeAt(index + 1)
                }
            } else {
                line.accept(this)
            }
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
        for (case in stmt.cases)
            case.body.accept(this)
        stmt.default?.accept(this)
    }
}