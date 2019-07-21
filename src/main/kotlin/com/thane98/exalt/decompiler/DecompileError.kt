package com.thane98.exalt.decompiler

import java.lang.Exception

class DecompileError(message: String, val address: Int): Exception(message)