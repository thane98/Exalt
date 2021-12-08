package com.thane98.exalt.error

import com.thane98.exalt.model.CompilerErrorLog

class AggregatedCompilerError(val errorLog: CompilerErrorLog) : Exception() {
    fun errors(): List<CompilerError> {
        return errorLog.errors
    }
}