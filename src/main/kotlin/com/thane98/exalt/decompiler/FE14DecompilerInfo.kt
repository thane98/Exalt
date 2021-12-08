package com.thane98.exalt.decompiler

import com.thane98.exalt.interfaces.CodeDecompilerFactory
import com.thane98.exalt.interfaces.DecompilerInfo
import com.thane98.exalt.interfaces.FunctionHeaderReader
import com.thane98.exalt.model.VersionInfo

class FE14DecompilerInfo(
    override val versionInfo: VersionInfo = VersionInfo.v3(),
    override val codeDecompilerFactory: CodeDecompilerFactory = V3CodeDecompilerFactory(),
    override val functionReader: FunctionHeaderReader = V3FunctionHeaderReader(FE14ArgReader())
) : DecompilerInfo