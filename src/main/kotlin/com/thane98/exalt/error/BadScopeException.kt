package com.thane98.exalt.error

class BadScopeException: Exception("Attempted to define symbol in a scope that is not active.")