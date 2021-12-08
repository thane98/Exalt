package com.thane98.exalt.compiler.parselet

import com.thane98.exalt.common.SymbolTable
import com.thane98.exalt.compiler.Parser
import com.thane98.exalt.error.CompilerError
import com.thane98.exalt.interfaces.PrefixParselet
import com.thane98.exalt.model.SourcePosition
import com.thane98.exalt.model.TokenType
import com.thane98.exalt.model.expr.ArrayRef
import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.expr.Funcall
import com.thane98.exalt.model.expr.VarRef
import com.thane98.exalt.model.symbol.FunctionSymbol
import com.thane98.exalt.model.symbol.VarSymbol

class IdentifierParselet(
    private val symbolTable: SymbolTable,
) : PrefixParselet {
    override fun parse(parser: Parser): Expr {
        val identifier = parser.previous.identifier!!
        if (parser.match(TokenType.LPAREN))
            return parseFuncall(parser.previous.position, identifier, parser)

        val sym = symbolTable.lookup(identifier)
        val varSym = sym as? VarSymbol ?: throw CompilerError.at(parser.previous.position,"Expected variable.")
        return if (parser.match(TokenType.LBRACKET)) {
            val index = parser.parseExpr()
            parser.consume(TokenType.RBRACKET)
            ArrayRef(varSym, index)
        } else {
            VarRef(varSym)
        }
    }

    private fun parseFuncall(anchor: SourcePosition, identifier: String, parser: Parser): Funcall {
        val args = mutableListOf<Expr>()
        if (!parser.check(TokenType.RPAREN)) {
            do {
                args.add(parser.parseExpr())
            } while (parser.match(TokenType.COMMA))
        }
        parser.consume(TokenType.RPAREN)

        var symbol = getFunctionSymbol(anchor, identifier)
        if (symbol == null) {
            symbol = FunctionSymbol(identifier, args.size)
            symbolTable.defineTopLevel(symbol)
        }
        return Funcall(symbol, args)
    }

    private fun getFunctionSymbol(anchor: SourcePosition, identifier: String): FunctionSymbol? {
        return when (val symbol = symbolTable.lookupOrNull(identifier)) {
            null -> {
                null
            }
            !is FunctionSymbol -> {
                throw CompilerError.at(anchor, "Expected function.")
            }
            else -> {
                symbol
            }
        }
    }
}