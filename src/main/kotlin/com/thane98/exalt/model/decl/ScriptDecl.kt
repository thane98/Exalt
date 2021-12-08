package com.thane98.exalt.model.decl

import com.thane98.exalt.interfaces.DeclVisitor
import com.thane98.exalt.model.Game

data class ScriptDecl(val game: Game, val scriptType: Int): Decl {
    override fun <T> accept(visitor: DeclVisitor<T>): T {
        return visitor.visitScriptDecl(this)
    }
}
