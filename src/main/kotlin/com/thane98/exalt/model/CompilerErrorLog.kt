package com.thane98.exalt.model

import com.thane98.exalt.error.CompilerError

class CompilerErrorLog {
    val errors = mutableListOf<CompilerError>()
    val isFailedRun: Boolean
        get() = errors.isNotEmpty()

    fun addError(error: CompilerError) {
        errors.add(error)
    }

    fun dump(): String {
        return errors.joinToString(System.lineSeparator() + System.lineSeparator()) { it.toString() }
    }
}