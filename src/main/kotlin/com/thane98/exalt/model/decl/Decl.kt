package com.thane98.exalt.model.decl

import com.thane98.exalt.interfaces.DeclVisitor

interface Decl {
    fun <T> accept(visitor: DeclVisitor<T>): T
}