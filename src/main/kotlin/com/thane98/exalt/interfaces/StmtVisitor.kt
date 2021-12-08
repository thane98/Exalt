package com.thane98.exalt.interfaces

import com.thane98.exalt.model.stmt.*

interface StmtVisitor<T> {
    fun visitBlock(stmt: Block): T
    fun visitExprStmt(stmt: ExprStmt): T
    fun visitFor(stmt: For): T
    fun visitGoto(stmt: Goto): T
    fun visitIf(stmt: If): T
    fun visitLabel(stmt: Label): T
    fun visitMatch(stmt: Match): T
    fun visitReturn(stmt: Return): T
    fun visitWhile(stmt: While): T
    fun visitYield(stmt: Yield): T
}