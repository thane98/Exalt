package decompiler

import java.lang.Exception

class DecompileError(message: String, val address: Int): Exception(message)