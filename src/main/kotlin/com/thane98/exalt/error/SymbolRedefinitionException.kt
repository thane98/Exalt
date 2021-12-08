package com.thane98.exalt.error

class SymbolRedefinitionException(name: String) : Exception("Symbol '$name' is already defined in this context.")