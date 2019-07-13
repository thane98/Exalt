package com.thane98.compiler

import com.thane98.ast.*
import com.thane98.common.Precedence
import com.thane98.common.TokenType

interface PrefixParselet {
    fun parse(op: TokenType, parser: Parser): Expr
}

interface InfixParselet {
    fun parse(op: TokenType, lhs: Expr, parser: Parser): Expr
    fun precedence(): Precedence
}

class LiteralParselet : PrefixParselet {
    override fun parse(op: TokenType, parser: Parser): Expr {
        assert(parser.previous.literal != null)
        return Literal(parser.previous.literal!!)
    }
}

class PrefixOperatorParselet : PrefixParselet {
    override fun parse(op: TokenType, parser: Parser): Expr {
        return UnaryExpr(op, parser.parseExpr(Precedence.PREFIX))
    }
}

class PreincrementParselet : PrefixParselet {
    override fun parse(op: TokenType, parser: Parser): Expr {
        val expr = parser.parseExpr(Precedence.PREFIX) as? Ref
            ?: throw CompileError("Expected reference.", parser.previous.pos)
        return Increment(op, expr, true)
    }

}

class GroupedParselet : PrefixParselet {
    override fun parse(op: TokenType, parser: Parser): Expr {
        val expr = parser.parseExpr()
        parser.consume(TokenType.RPAREN)
        return GroupedExpr(expr)
    }
}

class ReservedFunctionParselet : PrefixParselet {
    override fun parse(op: TokenType, parser: Parser): Expr {
        parser.consume(TokenType.LPAREN)
        val expr = parser.parseExpr()
        parser.consume(TokenType.RPAREN)
        return UnaryExpr(op, expr)
    }
}

class AsPointerParselet : PrefixParselet {
    override fun parse(op: TokenType, parser: Parser): Expr {
        val expr = parser.parseExpr(Precedence.PREFIX) as? Ref ?: throw CompileError(
            "Expected reference.",
            parser.previous.pos
        )
        expr.isPointer = true
        return expr
    }
}

class MinusParselet : PrefixParselet {
    override fun parse(op: TokenType, parser: Parser): Expr {
        if (parser.match(TokenType.INT))
            return Literal(-(parser.previous.literal as Int))
        return UnaryExpr(op, parser.parseExpr(Precedence.PREFIX))
    }
}

class FrameRefParselet : PrefixParselet {
    override fun parse(op: TokenType, parser: Parser): Expr {
        parser.consume(TokenType.INT)
        val frameID = parser.previous.literal as Int
        if (frameID >= parser.usedVars.size)
            throw CompileError("Frame reference exceeds frame size (256).", parser.previous.pos)
        parser.usedVars[frameID] = true

        val sym = VarSymbol("", frameID)
        if (parser.match(TokenType.LBRACKET))
            return parseArrayRef(sym, parser)
        return VarRef(sym)
    }
}

private fun parseArrayRef(base: VarSymbol, parser: Parser): ArrayRef {
    val index = parser.parseExpr()
    parser.consume(TokenType.RBRACKET)
    if (index is Literal && index.value is Int && base.frameID != -1) {
        val frameID = base.frameID + index.value
        if (frameID >= parser.usedVars.size)
            throw CompileError("Frame reference exceeds frame size (256).", parser.previous.pos)
        parser.usedVars[frameID] = true
    }
    return ArrayRef(base, index)
}

class IdentifierParselet : PrefixParselet {
    override fun parse(op: TokenType, parser: Parser): Expr {
        val ident = parser.previous
        if (parser.match(TokenType.LPAREN))
            return parseFuncall(ident, parser)

        val sym = parser.symbolTable.get(ident, ident.literal as String)
        if (sym is Constant)
            return sym.value

        val varSym = sym as? VarSymbol ?: throw CompileError("Expected variable.", parser.previous.pos)
        if (parser.match(TokenType.LBRACKET))
            return parseArrayRef(sym, parser)
        return VarRef(varSym)
    }

    private fun parseFuncall(ident: Token, parser: Parser): Funcall {
        val args = mutableListOf<Expr>()
        if (!parser.check(TokenType.RPAREN)) {
            do {
                args.add(parser.parseExpr())
            } while (parser.match(TokenType.COMMA))
        }
        parser.consume(TokenType.RPAREN)

        val result = Funcall(ident.literal as String, args)
        val sym = tryGetFuncSym(ident, parser)
        if (sym != null)
            result.callID = sym.id
        else {
            val calls = parser.unresolvedCalls[ident.literal]
            if (calls == null)
                parser.unresolvedCalls[ident.literal] = mutableListOf(result)
            else
                calls.add(result)
        }
        return result
    }

    private fun tryGetFuncSym(ident: Token, parser: Parser): EventSymbol? {
        if (parser.symbolTable.defined(ident.literal as String)) {
            val sym = parser.symbolTable.get(ident, ident.literal)
            if (sym is EventSymbol)
                return sym
        }
        return null
    }
}

class InfixOperatorParselet(private val precedence: Precedence) : InfixParselet {
    override fun precedence(): Precedence {
        return precedence
    }

    override fun parse(op: TokenType, lhs: Expr, parser: Parser): Expr {
        return BinaryExpr(lhs, op, parser.parseExpr(precedence()))
    }
}

class PostincrementParselet : InfixParselet {
    override fun precedence(): Precedence {
        return Precedence.POSTFIX
    }

    override fun parse(op: TokenType, lhs: Expr, parser: Parser): Expr {
        if (lhs !is Ref)
            throw CompileError("Expected reference.", parser.previous.pos)
        return Increment(op, lhs, false)
    }
}

class AssignmentParselet : InfixParselet {
    override fun parse(op: TokenType, lhs: Expr, parser: Parser): Expr {
        if (lhs !is Ref)
            throw CompileError("Left hand side of assignment must be a reference.", parser.previous.pos)
        return BinaryExpr(lhs, op, parser.parseExpr(Precedence.ASSIGNMENT))
    }

    override fun precedence(): Precedence {
        return Precedence.ASSIGNMENT
    }
}
