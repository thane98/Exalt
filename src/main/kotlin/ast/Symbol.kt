package ast

open class Symbol(val name: String)

class LabelSymbol(name: String, var address: Int = -1) : Symbol(name)

class VarSymbol(name: String, var frameID: Int = -1) : Symbol(name)

class Constant(name: String, val value: Literal) : Symbol(name)

class EventSymbol(name: String, val id: Int, val type: Int, val arity: Int, val numVars: Int) : Symbol(name)