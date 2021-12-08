package com.thane98.exalt.model.stmt

import com.thane98.exalt.interfaces.StmtVisitor

interface Stmt {
    fun <T> accept(visitor: StmtVisitor<T>): T
}