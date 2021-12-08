package com.thane98.exalt.error

class SymbolNotFoundException(name: String) : Exception("Symbol '$name' is not defined in this context.") {
}