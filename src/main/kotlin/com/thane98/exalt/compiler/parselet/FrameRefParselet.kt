package com.thane98.exalt.compiler.parselet

import com.thane98.exalt.common.SymbolTable
import com.thane98.exalt.compiler.Parser
import com.thane98.exalt.error.CompilerError
import com.thane98.exalt.interfaces.PrefixParselet
import com.thane98.exalt.model.TokenType
import com.thane98.exalt.model.expr.ArrayRef
import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.expr.VarRef
import com.thane98.exalt.model.symbol.VarSymbol

class FrameRefParselet(
    private val symbolTable: SymbolTable,
    private val global: Boolean,
) : PrefixParselet {
    override fun parse(parser: Parser): Expr {
        // Get the frame index/id
        parser.consume(TokenType.INT)
        val frameId = parser.previousValue.intValue()
        if (frameId >= Short.MAX_VALUE) {
            throw CompilerError.at(
                parser.previous.position,
                "Frame reference exceeds frame size (${Short.MAX_VALUE})."
            )
        }

        // Build the reference
        val name = VarSymbol.anonymousName(frameId)
        var symbol = symbolTable.lookupOrNull(name) as? VarSymbol
        if (symbol == null) {
            symbol = VarSymbol(name, global, frameId)

            // Defined in function scope since we're reserving a spot in the
            // frame for the entirety of the function.
            symbolTable.defineInFunctionScope(symbol)
        }
        return if (parser.match(TokenType.LBRACKET)) {
            val index = parser.parseExpr()
            parser.consume(TokenType.RBRACKET)
            ArrayRef(symbol, index)
        }
        else {
            VarRef(symbol)
        }
    }
}