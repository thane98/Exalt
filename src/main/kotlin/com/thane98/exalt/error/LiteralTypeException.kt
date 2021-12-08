package com.thane98.exalt.error

import com.thane98.exalt.model.LiteralType

class LiteralTypeException(expected: LiteralType) : Exception("Expected literal to hold type $expected.")