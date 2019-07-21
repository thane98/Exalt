package com.thane98.exalt.compiler

import com.thane98.exalt.common.TokenType

data class SourcePosition(val filePath: String?, val lineNumber: Int, val column: Int, val length: Int)

data class Token(val type: TokenType, val pos: SourcePosition, val literal: Any? = null)