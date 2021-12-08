package com.thane98.exalt.model.decl

import com.thane98.exalt.interfaces.DeclVisitor
import com.thane98.exalt.model.expr.Literal
import com.thane98.exalt.model.stmt.Block

data class EventDecl(
    val eventType: Int,
    val args: List<Literal>,
    val body: Block,
    val annotations: MutableList<Annotation>,
    var frameSize: Int = -1,
): Decl {
    override fun <T> accept(visitor: DeclVisitor<T>): T {
        return visitor.visitEventDecl(this)
    }
}