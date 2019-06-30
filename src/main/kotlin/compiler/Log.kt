package compiler

import java.lang.StringBuilder

class Log(private val sourceManager: SourceManager) {
    private val errors = mutableListOf<CompileError>()

    fun hasErrors(): Boolean {
        return errors.isNotEmpty()
    }

    fun logError(error: CompileError) {
        errors.add(error)
    }

    fun dump(): String {
        val sb = StringBuilder()
        for (error in errors) {
            sb.append(format(error))
            if (error != errors.last())
                sb.append('\n')
        }
        return sb.toString()
    }

    private fun format(error: CompileError): String {
        // Append base message (filename and error message).
        val sb = StringBuilder()
        val pos = error.position
        if (pos.filePath != null && pos.filePath != SourceManager.IN_MEMORY_SOURCE_PATH)
            sb.append(pos.filePath).append('\n')
        sb.append(error.message).append('\n')

        // Append positional information.
        if (pos.column != -1 && pos.lineNumber != -1) {
            // Append source line.
            assert(pos.filePath != null)
            val sourceLine = sourceManager.sourceFor(pos.filePath!!)[pos.lineNumber - 1]
            val lineString = pos.lineNumber.toString()
            sb.append("$lineString | ").append(sourceLine).append('\n')

            // Append position caret.
            if (pos.column < sourceLine.length) {
                for (i in 0 until (pos.column + lineString.length + 3))
                    sb.append(' ')
                sb.append("^\n")
            }
        }
        return sb.toString()
    }
}