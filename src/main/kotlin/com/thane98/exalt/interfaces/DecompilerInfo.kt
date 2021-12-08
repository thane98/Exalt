package com.thane98.exalt.interfaces

import com.thane98.exalt.model.VersionInfo

interface DecompilerInfo {
    val versionInfo: VersionInfo
    val codeDecompilerFactory: CodeDecompilerFactory
    val functionReader: FunctionHeaderReader
}