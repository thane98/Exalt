package com.thane98.exalt.error

import com.thane98.exalt.model.SourcePosition

class CompilerError private constructor(val position: SourcePosition, message: String, inner: Exception?) :
    Exception(message, inner) {

    companion object {
        fun at(pos: SourcePosition, message: String): CompilerError {
            if (pos.isUndefined()) {
                return CompilerError(pos, message, null)
            }
            val sb = StringBuilder()
                .appendLine(message)
                .append(pos.file)
                .append(": line ")
                .append(pos.lineNumber + 1)
                .append(", ")
                .append("index ")
                .appendLine(pos.index)
                .appendLine(pos.line)
            return CompilerError(pos, sb.toString(), null)
        }

        fun at(pos: SourcePosition, inner: Exception): CompilerError {
            if (pos.isUndefined()) {
                return CompilerError(pos, "${inner.message}", inner)
            }
            val sb = StringBuilder()
                .appendLine(inner.message)
                .append(pos.file)
                .append(": line ")
                .append(pos.lineNumber + 1)
                .append(", ")
                .append("index ")
                .appendLine(pos.index)
                .appendLine(pos.line)
            return CompilerError(pos, sb.toString(), inner)
        }
    }
}