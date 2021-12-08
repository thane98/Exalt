package com.thane98.exalt.model

data class SourcePosition(val file: String, val line: String, val lineNumber: Int, val index: Int) {
    fun isUndefined(): Boolean {
        return lineNumber == -1
    }
}