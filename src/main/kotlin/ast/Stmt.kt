package ast

abstract class Stmt(var address: Int = -1) {
    abstract fun <T> accept(visitor: StmtVisitor<T>): T
}

interface StmtVisitor<T> {
    fun visitBlock(stmt: Block): T
    fun visitFuncDecl(stmt: FuncDecl): T
    fun visitEventDecl(stmt: EventDecl): T
    fun visitIf(stmt: If): T
    fun visitLabel(stmt: Label): T
    fun visitGoto(stmt: Goto): T
    fun visitExprStmt(stmt: ExprStmt): T
    fun visitReturn(stmt: Return): T
    fun visitYield(stmt: Yield): T
    fun visitWhile(stmt: While): T
    fun visitFor(stmt: For): T
    fun visitMatch(stmt: Match): T
}

class Block(val contents: MutableList<Stmt>) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitBlock(this)
    }
}

open class FuncDecl(val symbol: EventSymbol, val contents: Block) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitFuncDecl(this)
    }
}

class EventDecl(symbol: EventSymbol, contents: Block, val args: List<Literal>) : FuncDecl(symbol, contents) {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitEventDecl(this)
    }
}

class If(val cond: Expr, val thenPart: Stmt, val elsePart: Stmt?) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitIf(this)
    }
}

class Label(val symbol: LabelSymbol) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitLabel(this)
    }
}

class Goto(var target: LabelSymbol?) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitGoto(this)
    }
}

class ExprStmt(val expr: Expr) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitExprStmt(this)
    }
}

class Return(val value: Expr? = null) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitReturn(this)
    }
}

class Yield : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitYield(this)
    }
}

class While(val cond: Expr, val body: Stmt) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitWhile(this)
    }
}

class For(val init: Stmt?, val check: Expr, val step: Stmt?, val body: Stmt) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitFor(this)
    }
}

class Match(val switch: Expr, val cases: List<Case>, val default: Stmt? = null) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T {
        return visitor.visitMatch(this)
    }
}

data class Case(val cond: Expr, val body: Stmt)