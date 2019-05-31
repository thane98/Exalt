package compiler

import java.lang.Exception

data class CompileError(val msg: String, val position: SourcePosition) : Exception(msg)