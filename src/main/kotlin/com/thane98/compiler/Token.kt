package com.thane98.compiler

import com.thane98.common.TokenType

data class SourcePosition(val filePath: String?, val lineNumber: Int, val column: Int)

data class Token(val type: TokenType, val pos: SourcePosition, val literal: Any? = null)