package com.thane98.exalt.common

import com.thane98.exalt.interfaces.DeclVisitor
import com.thane98.exalt.model.decl.*
import com.thane98.exalt.model.decl.Annotation

class AnnotationAppender(
    private val annotation: Annotation
) : DeclVisitor<Unit> {
    companion object {
        fun append(decl: Decl, annotation: Annotation) {
            decl.accept(AnnotationAppender(annotation))
        }
    }

    override fun visitScriptDecl(decl: ScriptDecl) {
        throw IllegalArgumentException("Annotations can only be used with events or functions.")
    }

    override fun visitEventDecl(decl: EventDecl) {
        decl.annotations.add(annotation)
    }

    override fun visitFunctionDecl(decl: FunctionDecl) {
        decl.annotations.add(annotation)
    }
}