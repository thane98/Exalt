package com.thane98.exalt.decompiler

import com.thane98.exalt.interfaces.CodeDecompilerFactory
import com.thane98.exalt.interfaces.DecompilerInfo
import com.thane98.exalt.interfaces.FunctionHeaderReader
import com.thane98.exalt.model.VersionInfo

class FE10DecompilerInfo(
    override val versionInfo: VersionInfo = VersionInfo.v2(),
    override val codeDecompilerFactory: CodeDecompilerFactory = V2CodeDecompilerFactory(),
    override val functionReader: FunctionHeaderReader = V1FunctionHeaderReader(FE10ArgReader())
) : DecompilerInfo