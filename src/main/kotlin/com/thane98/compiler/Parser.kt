package com.thane98.compiler

import com.thane98.ast.*
import com.thane98.common.Precedence
import com.thane98.common.TokenType

class Parser(private val tokens: List<Token>, private val log: Log) {
    private val unresolvedGotos = hashMapOf<String, MutableList<Pair<Token, Goto>>>()
    private val unresolvedVars = mutableListOf<Pair<VarSymbol, Int>>()
    internal val unresolvedCalls = hashMapOf<String, MutableList<Funcall>>()
    internal val symbolTable = SymbolTable()
    internal val usedVars = BooleanArray(256)
    private var nextEventID = 0
    private var position = 0
    private val atEnd: Boolean
        get() = tokens[position].type == TokenType.EOF

    companion object {
        private val DECL_BEGINNINGS = hashSetOf(
            TokenType.EVENT, TokenType.FUNC, TokenType.CONST
        )

        private val STMT_BEGINNINGS = hashSetOf(
            TokenType.VAR, TokenType.LET, TokenType.GOTO,
            TokenType.LABEL, TokenType.MATCH, TokenType.IF,
            TokenType.FOR, TokenType.WHILE, TokenType.YIELD,
            TokenType.RETURN, TokenType.RBRACE
        )

        private val PREFIX_ACTIONS = hashMapOf(
            TokenType.NOT to PrefixOperatorParselet(),
            TokenType.BNOT to PrefixOperatorParselet(),
            TokenType.INC to PreincrementParselet(),
            TokenType.DEC to PreincrementParselet(),
            TokenType.FMINUS to MinusParselet(),
            TokenType.MINUS to MinusParselet(),
            TokenType.IDENTIFIER to IdentifierParselet(),
            TokenType.FRAME_REF to FrameRefParselet(),
            TokenType.BAND to AsPointerParselet(),
            TokenType.LPAREN to GroupedParselet(),
            TokenType.FIX to ReservedFunctionParselet(),
            TokenType.FLOAT to ReservedFunctionParselet(),
            TokenType.INT to LiteralParselet(),
            TokenType.REAL to LiteralParselet(),
            TokenType.STRING to LiteralParselet()
        )

        private val INFIX_ACTIONS = hashMapOf(
            TokenType.PLUS to InfixOperatorParselet(Precedence.ADDITION),
            TokenType.MINUS to InfixOperatorParselet(Precedence.ADDITION),
            TokenType.TIMES to InfixOperatorParselet(Precedence.MULTIPLICATION),
            TokenType.DIVIDE to InfixOperatorParselet(Precedence.MULTIPLICATION),
            TokenType.MODULO to InfixOperatorParselet(Precedence.MULTIPLICATION),
            TokenType.FPLUS to InfixOperatorParselet(Precedence.ADDITION),
            TokenType.FMINUS to InfixOperatorParselet(Precedence.ADDITION),
            TokenType.FTIMES to InfixOperatorParselet(Precedence.MULTIPLICATION),
            TokenType.FDIVIDE to InfixOperatorParselet(Precedence.MULTIPLICATION),
            TokenType.EQ to InfixOperatorParselet(Precedence.EQUALITY),
            TokenType.NE to InfixOperatorParselet(Precedence.EQUALITY),
            TokenType.LT to InfixOperatorParselet(Precedence.COMPARISON),
            TokenType.LE to InfixOperatorParselet(Precedence.COMPARISON),
            TokenType.GT to InfixOperatorParselet(Precedence.COMPARISON),
            TokenType.GE to InfixOperatorParselet(Precedence.COMPARISON),
            TokenType.FEQ to InfixOperatorParselet(Precedence.EQUALITY),
            TokenType.FNE to InfixOperatorParselet(Precedence.EQUALITY),
            TokenType.FLT to InfixOperatorParselet(Precedence.COMPARISON),
            TokenType.FLE to InfixOperatorParselet(Precedence.COMPARISON),
            TokenType.FGT to InfixOperatorParselet(Precedence.COMPARISON),
            TokenType.FGE to InfixOperatorParselet(Precedence.COMPARISON),
            TokenType.RS to InfixOperatorParselet(Precedence.SHIFT),
            TokenType.LS to InfixOperatorParselet(Precedence.SHIFT),
            TokenType.BAND to InfixOperatorParselet(Precedence.BAND),
            TokenType.BOR to InfixOperatorParselet(Precedence.BOR),
            TokenType.XOR to InfixOperatorParselet(Precedence.XOR),
            TokenType.AND to InfixOperatorParselet(Precedence.AND),
            TokenType.OR to InfixOperatorParselet(Precedence.OR),
            TokenType.INC to PostincrementParselet(),
            TokenType.DEC to PostincrementParselet(),
            TokenType.ASSIGN to AssignmentParselet(),
            TokenType.ASSIGN_PLUS to AssignmentParselet(),
            TokenType.ASSIGN_MINUS to AssignmentParselet(),
            TokenType.ASSIGN_TIMES to AssignmentParselet(),
            TokenType.ASSIGN_DIVIDE to AssignmentParselet(),
            TokenType.ASSIGN_FPLUS to AssignmentParselet(),
            TokenType.ASSIGN_FMINUS to AssignmentParselet(),
            TokenType.ASSIGN_FTIMES to AssignmentParselet(),
            TokenType.ASSIGN_FDIVIDE to AssignmentParselet()
        )
    }

    fun parse(): Block {
        val decls = mutableListOf<Stmt>()
        while (!atEnd) {
            try {
                val decl = parseDecl()
                if (decl != null)
                    decls.add(decl)
            } catch (error: CompileError) {
                log.logError(error)
                synchronizeToDecl()
            }
        }
        return Block(decls)
    }

    private fun parseDecl(): Stmt? {
        return when (peek().type) {
            TokenType.EVENT -> parseEvent()
            TokenType.FUNC -> parseFunc()
            TokenType.CONST -> parseConst()
            else -> throw CompileError("Expected declaration.", peek().pos)
        }
    }

    private fun parseEvent(): Stmt {
        setupForEventOrFunc()
        consume(TokenType.EVENT, TokenType.LBRACKET, TokenType.INT)
        val type = previous.literal as Int
        consume(TokenType.RBRACKET, TokenType.LPAREN)
        val args = parseEventArgs()
        consume(TokenType.RPAREN)
        val body = parseBlock()
        return EventDecl(finishEventOrFunc("", type, args.size), body, args)
    }

    private fun setupForEventOrFunc() {
        usedVars.fill(false)
        unresolvedVars.clear()
        unresolvedGotos.clear()
    }

    private fun finishEventOrFunc(name: String, type: Int, arity: Int): EventSymbol {
        if (unresolvedGotos.isNotEmpty()) {
            for (entry in unresolvedGotos.values)
                log.logError(CompileError("Unresolved goto.", entry.first().first.pos))
        }
        val numVars = allocateVars()
        return EventSymbol(name, nextEventID++, type, arity, numVars)
    }

    private fun allocateVars(): Int {
        var start = 0
        for (entry in unresolvedVars) {
            val blockIndex = findEmptyBlock(start, entry.second)
            for (i in blockIndex until blockIndex + entry.second)
                usedVars[i] = true
            start += entry.second
            entry.first.frameID = blockIndex
        }
        val frameSize = usedVars.lastIndexOf(true)
        return if (frameSize != -1) frameSize + 1 else 0
    }

    private fun findEmptyBlock(start: Int, size: Int): Int {
        var i = start
        while (i < usedVars.size) {
            var fits = true
            var j = i
            while (fits && j < i + size) {
                if (usedVars[j]) {
                    fits = false
                    i = j + 1
                }
                j++
            }
            if (fits)
                return i
        }
        throw CompileError("Unable to allocate space for all variables.", previous.pos)
    }

    private fun parseEventArgs(): List<Literal> {
        val result = mutableListOf<Literal>()
        if (!check(TokenType.RPAREN)) {
            do {
                val expr = parseExpr() as? Literal ?: throw CompileError("Expected constant.", previous.pos)
                result.add(expr)
            } while (match(TokenType.COMMA))
        }
        return result
    }

    private fun parseFunc(): Stmt {
        setupForEventOrFunc()
        symbolTable.enterScope()
        consume(TokenType.FUNC, TokenType.IDENTIFIER)
        val ident = previous
        consume(TokenType.LPAREN)
        val params = parseFuncParameters()
        consume(TokenType.RPAREN)
        val body = parseBlock()

        val sym = finishEventOrFunc(ident.literal as String, 0, params.size)
        val calls = unresolvedCalls[ident.literal]
        if (calls != null) {
            for (call in calls)
                call.callID = sym.id
            unresolvedCalls.remove(ident.literal)
        }
        symbolTable.exitScope()
        symbolTable.define(ident, sym)
        return FuncDecl(sym, body, params)
    }

    private fun parseFuncParameters(): List<VarSymbol> {
        val result = mutableListOf<VarSymbol>()
        if (!check(TokenType.RPAREN)) {
            do {
                val isExternal = match(TokenType.BAND)
                consume(TokenType.IDENTIFIER)
                usedVars[result.size] = true
                val sym = VarSymbol(previous.literal as String, result.size)
                sym.isExternal = isExternal
                symbolTable.define(previous, sym)
                result.add(sym)
            } while (match(TokenType.COMMA))
        }
        return result
    }

    private fun parseConst(): Stmt? {
        consume(TokenType.CONST, TokenType.IDENTIFIER)
        val ident = previous
        consume(TokenType.ASSIGN)
        val rhs = parseExpr() as? Literal ?: throw CompileError("Expected constant.", previous.pos)
        consume(TokenType.SEMICOLON)
        symbolTable.define(ident, Constant(ident.literal as String, rhs))
        return null
    }

    internal fun parseExpr(precedence: Precedence = Precedence.NONE): Expr {
        val token = advance()
        val prefixAction = PREFIX_ACTIONS[token.type] ?: throw CompileError(
            "Expected expression.",
            token.pos
        )

        var expr = prefixAction.parse(token.type, this)
        while (precedence < precedenceOfNext()) {
            val next = advance()
            val infixAction = INFIX_ACTIONS[next.type]!!
            expr = infixAction.parse(next.type, expr, this)
        }
        return expr
    }

    private fun precedenceOfNext(): Precedence {
        if (INFIX_ACTIONS.containsKey(peek().type))
            return INFIX_ACTIONS[peek().type]!!.precedence()
        return Precedence.NONE
    }

    private fun parseBlock(): Block {
        symbolTable.enterScope()
        consume(TokenType.LBRACE)
        val contents = mutableListOf<Stmt>()
        while (!match(TokenType.RBRACE)) {
            try {
                val stmt = parseStmt()
                if (stmt != null)
                    contents.add(stmt)
            } catch (error: CompileError) {
                log.logError(error)
                synchronizeToStmt()
            }
        }
        symbolTable.exitScope()
        return Block(contents)
    }

    private fun parseStmt(): Stmt? {
        return when (peek().type) {
            TokenType.YIELD -> parseYield()
            TokenType.IF -> parseIf()
            TokenType.RETURN -> parseReturn()
            TokenType.WHILE -> parseWhile()
            TokenType.FOR -> parseFor()
            TokenType.MATCH -> parseMatch()
            TokenType.LET -> parseLet()
            TokenType.VAR -> parseVar()
            TokenType.LABEL -> parseLabel()
            TokenType.GOTO -> parseGoto()
            TokenType.LBRACE -> parseBlock()
            else -> parseExprStmt()
        }
    }

    private fun parseConcreteStmt(): Stmt {
        return parseStmt() ?: throw CompileError("Expected concrete statement.", previous.pos)
    }

    private fun parseYield(): Yield {
        consume(TokenType.YIELD, TokenType.LPAREN, TokenType.RPAREN, TokenType.SEMICOLON)
        return Yield()
    }

    private fun parseIf(): If {
        consume(TokenType.IF, TokenType.LPAREN)
        val cond = parseExpr()
        consume(TokenType.RPAREN)
        val thenPart = parseConcreteStmt()
        var elsePart: Stmt? = null
        if (match(TokenType.ELSE))
            elsePart = parseConcreteStmt()
        return If(cond, thenPart, elsePart)
    }

    private fun parseReturn(): Return {
        consume(TokenType.RETURN)
        var value: Expr? = null
        if (!check(TokenType.SEMICOLON))
            value = parseExpr()
        consume(TokenType.SEMICOLON)
        return Return(value)
    }

    private fun parseWhile(): While {
        consume(TokenType.WHILE, TokenType.LPAREN)
        val cond = parseExpr()
        consume(TokenType.RPAREN)
        val body = parseConcreteStmt()
        return While(cond, body)
    }

    private fun parseFor(): For {
        consume(TokenType.FOR, TokenType.LPAREN)
        symbolTable.enterScope()
        val init = parseForInit()
        val check = parseForCheck()
        consume(TokenType.SEMICOLON)
        val step = parseForStep()
        consume(TokenType.RPAREN)
        val body = parseConcreteStmt()
        symbolTable.exitScope()
        return For(init, check, step, body)
    }

    private fun parseForInit(): Stmt? {
        return when (peek().type) {
            TokenType.VAR -> parseVar()
            TokenType.SEMICOLON -> {
                consume(TokenType.SEMICOLON)
                null
            }
            else -> parseExprStmt()
        }
    }

    private fun parseForCheck(): Expr {
        if (!check(TokenType.SEMICOLON))
            return parseExpr()

        // No condition; produce an infinite loop.
        return Literal(1)
    }

    private fun parseForStep(): Stmt? {
        if (!check(TokenType.RPAREN))
            return ExprStmt(parseExpr())
        return null
    }

    private fun parseMatch(): Match {
        consume(TokenType.MATCH, TokenType.LPAREN)
        val cond = parseExpr()
        consume(TokenType.RPAREN, TokenType.LBRACE)

        val cases = mutableListOf<Case>()
        var default: Stmt? = null
        while (!match(TokenType.RBRACE)) {
            if (match(TokenType.ELSE)) {
                if (default != null)
                    throw CompileError("Cannot have multiple default cases in match.", previous.pos)
                consume(TokenType.ARROW)
                default = parseConcreteStmt()
            } else {
                val expr = parseExpr()
                consume(TokenType.ARROW)
                val body = parseConcreteStmt()
                cases.add(Case(expr, body))
            }
        }
        return Match(cond, cases, default)
    }

    private fun parseLabel(): Label {
        consume(TokenType.LABEL, TokenType.IDENTIFIER)
        val ident = previous
        consume(TokenType.SEMICOLON)

        // Create the symbol table entry for the label.
        val sym = LabelSymbol(ident.literal as String)
        symbolTable.defineLabel(ident, sym)

        // Backpatch any references to this label.
        val targets = unresolvedGotos[ident.literal]
        if (targets != null) {
            for (entry in targets)
                entry.second.target = sym
            unresolvedGotos.remove(ident.literal)
        }
        return Label(sym)
    }

    private fun parseGoto(): Goto {
        consume(TokenType.GOTO, TokenType.IDENTIFIER)
        val ident = previous
        consume(TokenType.SEMICOLON)

        var target: LabelSymbol? = null
        if (symbolTable.defined(ident.literal as String)) {
            val sym = symbolTable.get(ident, ident.literal)
            target = sym as? LabelSymbol ?: throw CompileError("Expected label symbol.", previous.pos)
        }

        val result = Goto(target)
        if (target == null) {
            val unresolved = unresolvedGotos[ident.literal]
            if (unresolved == null)
                unresolvedGotos[ident.literal] = mutableListOf(Pair(ident, result))
            else
                unresolved.add(Pair(ident, result))
        }
        return result
    }

    private fun parseLet(): Stmt? {
        // Parse the statement contents.
        consume(TokenType.LET, TokenType.IDENTIFIER)
        val ident = previous
        consume(TokenType.ASSIGN)

        // Force the right hand side to be a known variable reference.
        // Ex. Accept $2 but not a variable declared with "var".
        val rhs = parseExprStmt()
        val ref = rhs.expr as? VarRef
        if (ref == null || ref.symbol.frameID == -1)
            throw CompileError("Right hand side of let must be a known variable reference.", previous.pos)

        // Create an alias for the frame reference and add it to the symbol table.
        symbolTable.define(ident, VarSymbol(ident.literal as String, ref.symbol.frameID))
        return null
    }

    private fun parseVar(): Stmt? {
        // Parse left hand side
        consume(TokenType.VAR, TokenType.IDENTIFIER)
        val symbol = VarSymbol(previous.literal as String)
        symbolTable.define(previous, symbol)
        consume(TokenType.ASSIGN)
        return when (peek().type) {
            TokenType.ARRAY -> parseEmptyArrayInit(symbol)
            TokenType.LBRACKET -> parseStaticArrayInit(symbol)
            else -> parseNormalVarDecl(symbol)
        }
    }

    private fun parseNormalVarDecl(symbol: VarSymbol): Stmt? {
        unresolvedVars.add(Pair(symbol, 1))
        val assignment = BinaryExpr(VarRef(symbol), TokenType.ASSIGN, parseExpr())
        consume(TokenType.SEMICOLON)
        return ExprStmt(assignment)
    }

    private fun parseEmptyArrayInit(symbol: VarSymbol): Stmt? {
        consume(TokenType.ARRAY, TokenType.LBRACKET, TokenType.INT)
        unresolvedVars.add(Pair(symbol, previous.literal as Int))
        consume(TokenType.RBRACKET, TokenType.SEMICOLON)
        return null
    }

    private fun parseStaticArrayInit(symbol: VarSymbol): Stmt? {
        val initializers = mutableListOf<Stmt>()
        consume(TokenType.LBRACKET)
        if (!check(TokenType.RBRACKET)) {
            do {
                val expr = parseExpr() as? Literal ?: throw CompileError("Expected constant.", previous.pos)
                val ref = ArrayRef(symbol, Literal(initializers.size))
                initializers.add(ExprStmt(BinaryExpr(ref, TokenType.ASSIGN, expr)))
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RBRACKET, TokenType.SEMICOLON)
        unresolvedVars.add(Pair(symbol, initializers.size))
        return Block(initializers)
    }

    private fun parseExprStmt(): ExprStmt {
        val expr = parseExpr()
        consume(TokenType.SEMICOLON)
        return ExprStmt(expr)
    }

    internal val previous: Token
        get() = tokens[position - 1]

    private fun advance(): Token {
        assert(position < tokens.size)
        return tokens[position++]
    }

    private fun peek(): Token {
        if (atEnd)
            throw CompileError("Reached EOF while parsing.", tokens.last().pos)
        return tokens[position]
    }

    internal fun consume(type: TokenType) {
        if (!match(type)) {
            // Provide a more precise error message for semicolons.
            if (type == TokenType.SEMICOLON)
                throw CompileError("Expected semicolon after statement.", previous.pos)

            // General case. Show expected token and what we actually found.
            val actualTok = peek()
            val actual = peek().type.toString()
            val expected = type.toString()
            throw CompileError("Expected \"$expected\", found \"$actual\".", actualTok.pos)
        }
    }

    private fun consume(vararg types: TokenType) {
        for (type in types)
            consume(type)
    }

    internal fun check(type: TokenType): Boolean {
        return peek().type == type
    }

    internal fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun synchronizeToDecl() {
        while (!atEnd) {
            if (peek().type in DECL_BEGINNINGS)
                return
            advance()
        }
    }

    private fun synchronizeToStmt() {
        while (!atEnd) {
            if (match(TokenType.SEMICOLON) || peek().type in STMT_BEGINNINGS)
                return
            advance()
        }
    }
}