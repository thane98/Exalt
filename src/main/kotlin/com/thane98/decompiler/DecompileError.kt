package com.thane98.decompiler

import java.lang.Exception

class DecompileError(message: String, val address: Int): Exception(message)