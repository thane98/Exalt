package com.thane98.exalt.decompiler

import com.thane98.exalt.interfaces.CodeDecompilerFactory
import com.thane98.exalt.interfaces.DecompilerInfo
import com.thane98.exalt.interfaces.FunctionHeaderReader
import com.thane98.exalt.model.VersionInfo

class FE9DecompilerInfo(
    override val versionInfo: VersionInfo = VersionInfo.v1(),
    override val codeDecompilerFactory: CodeDecompilerFactory = V1CodeDecompilerFactory(),
    override val functionReader: FunctionHeaderReader = V1FunctionHeaderReader(FE9ArgReader())
) : DecompilerInfo