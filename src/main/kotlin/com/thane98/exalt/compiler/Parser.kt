package com.thane98.exalt.compiler

import com.thane98.exalt.common.SymbolTable
import com.thane98.exalt.compiler.parselet.*
import com.thane98.exalt.error.AggregatedCompilerError
import com.thane98.exalt.error.CompilerError
import com.thane98.exalt.error.SymbolRedefinitionException
import com.thane98.exalt.model.*
import com.thane98.exalt.model.decl.Annotation
import com.thane98.exalt.model.decl.Decl
import com.thane98.exalt.model.decl.EventDecl
import com.thane98.exalt.model.decl.FunctionDecl
import com.thane98.exalt.model.decl.ScriptDecl
import com.thane98.exalt.model.expr.*
import com.thane98.exalt.model.stmt.*
import com.thane98.exalt.model.symbol.FunctionSymbol
import com.thane98.exalt.model.symbol.LabelSymbol
import com.thane98.exalt.model.symbol.VarSymbol

class Parser(
    private val errorLog: CompilerErrorLog,
    private val tokens: List<Token>,
    var features: ParserFeatures = ParserFeatures(true),
) {
    private val symbolTable = SymbolTable()
    private val labelTracker = LabelTracker(symbolTable)
    private val atEnd: Boolean
        get() = tokens[position].type == TokenType.EOF

    private var position = 0
    private var hasScriptDecl = false

    private val stmtBeginnings = setOf(
        TokenType.VAR,
        TokenType.GOTO,
        TokenType.LABEL,
        TokenType.WHILE,
        TokenType.FOR,
        TokenType.MATCH,
        TokenType.YIELD,
        TokenType.RETURN,
        TokenType.LBRACE,
        // With this we assume that the next } will terminate the block.
        // Given that scripts are *usually* simple, this is more likely
        // than synchronizing to the wrong block closure.
        TokenType.RBRACE,
    )

    private val declBeginnings = setOf(
        TokenType.SCRIPT,
        TokenType.EVENT,
        TokenType.FUNC,
    )

    private val prefixActions = mapOf(
        TokenType.LOGICAL_NOT to PrefixOperatorParselet(Operator.LOGICAL_NOT),
        TokenType.BINARY_NOT to PrefixOperatorParselet(Operator.BINARY_NOT),
        TokenType.INCREMENT to PrefixIncrementParselet(Operator.INCREMENT),
        TokenType.DECREMENT to PrefixIncrementParselet(Operator.DECREMENT),
        TokenType.FMINUS to MinusParselet(Operator.FLOAT_NEGATE),
        TokenType.MINUS to MinusParselet(Operator.NEGATE),
        TokenType.IDENTIFIER to IdentifierParselet(symbolTable),
        TokenType.FRAME_REF to FrameRefParselet(symbolTable, false),
        TokenType.GLOBAL_FRAME_REF to FrameRefParselet(symbolTable, true),
        TokenType.AMPERSAND to PointerParselet(),
        TokenType.LPAREN to GroupedParselet(),
        TokenType.INT to LiteralParselet(),
        TokenType.FLOAT to LiteralParselet(),
        TokenType.STRING to LiteralParselet()
    )

    private val infixActions = mapOf(
        TokenType.PLUS to InfixOperatorParselet(Operator.ADD),
        TokenType.MINUS to InfixOperatorParselet(Operator.SUBTRACT),
        TokenType.TIMES to InfixOperatorParselet(Operator.MULTIPLY),
        TokenType.DIVIDE to InfixOperatorParselet(Operator.DIVIDE),
        TokenType.MODULO to InfixOperatorParselet(Operator.MODULO),
        TokenType.FPLUS to InfixOperatorParselet(Operator.FLOAT_ADD),
        TokenType.FMINUS to InfixOperatorParselet(Operator.FLOAT_SUBTRACT),
        TokenType.FTIMES to InfixOperatorParselet(Operator.FLOAT_MULTIPLY),
        TokenType.FDIVIDE to InfixOperatorParselet(Operator.FLOAT_DIVIDE),
        TokenType.EQ to InfixOperatorParselet(Operator.EQUAL),
        TokenType.NE to InfixOperatorParselet(Operator.NOT_EQUAL),
        TokenType.LT to InfixOperatorParselet(Operator.LESS_THAN),
        TokenType.LE to InfixOperatorParselet(Operator.LESS_THAN_OR_EQUAL_TO),
        TokenType.GT to InfixOperatorParselet(Operator.GREATER_THAN),
        TokenType.GE to InfixOperatorParselet(Operator.GREATER_THAN_OR_EQUAL_TO),
        TokenType.FEQ to InfixOperatorParselet(Operator.FLOAT_EQUAL),
        TokenType.FNE to InfixOperatorParselet(Operator.FLOAT_NOT_EQUAL),
        TokenType.FLT to InfixOperatorParselet(Operator.FLOAT_LESS_THAN),
        TokenType.FLE to InfixOperatorParselet(Operator.FLOAT_LESS_THAN_OR_EQUAL_TO),
        TokenType.FGT to InfixOperatorParselet(Operator.FLOAT_GREATER_THAN),
        TokenType.FGE to InfixOperatorParselet(Operator.FLOAT_GREATER_THAN_OR_EQUAL_TO),
        TokenType.RIGHT_SHIFT to InfixOperatorParselet(Operator.RIGHT_SHIFT),
        TokenType.LEFT_SHIFT to InfixOperatorParselet(Operator.LEFT_SHIFT),
        TokenType.AMPERSAND to InfixOperatorParselet(Operator.BINARY_AND),
        TokenType.BINARY_OR to InfixOperatorParselet(Operator.BINARY_OR),
        TokenType.XOR to InfixOperatorParselet(Operator.XOR),
        TokenType.LOGICAL_AND to InfixOperatorParselet(Operator.LOGICAL_AND),
        TokenType.LOGICAL_OR to InfixOperatorParselet(Operator.LOGICAL_OR),
        TokenType.INCREMENT to PostfixIncrementParselet(Operator.INCREMENT),
        TokenType.DECREMENT to PostfixIncrementParselet(Operator.DECREMENT),
        TokenType.ASSIGN to AssignmentParselet(Operator.ASSIGN),
        TokenType.ASSIGN_ADD to AssignmentParselet(Operator.ASSIGN_ADD),
        TokenType.ASSIGN_SUBTRACT to AssignmentParselet(Operator.ASSIGN_SUBTRACT),
        TokenType.ASSIGN_MULTIPLY to AssignmentParselet(Operator.ASSIGN_MULTIPLY),
        TokenType.ASSIGN_DIVIDE to AssignmentParselet(Operator.ASSIGN_DIVIDE),
        TokenType.ASSIGN_MODULO to AssignmentParselet(Operator.ASSIGN_MODULO),
        TokenType.ASSIGN_BINARY_OR to AssignmentParselet(Operator.ASSIGN_BINARY_OR),
        TokenType.ASSIGN_BINARY_AND to AssignmentParselet(Operator.ASSIGN_BINARY_AND),
        TokenType.ASSIGN_XOR to AssignmentParselet(Operator.ASSIGN_XOR),
        TokenType.ASSIGN_LEFT_SHIFT to AssignmentParselet(Operator.ASSIGN_LEFT_SHIFT),
        TokenType.ASSIGN_RIGHT_SHIFT to AssignmentParselet(Operator.ASSIGN_RIGHT_SHIFT),
        TokenType.ASSIGN_FLOAT_ADD to AssignmentParselet(Operator.ASSIGN_FLOAT_ADD),
        TokenType.ASSIGN_FLOAT_SUBTRACT to AssignmentParselet(Operator.ASSIGN_FLOAT_SUBTRACT),
        TokenType.ASSIGN_FLOAT_MULTIPLY to AssignmentParselet(Operator.ASSIGN_FLOAT_MULTIPLY),
        TokenType.ASSIGN_FLOAT_DIVIDE to AssignmentParselet(Operator.ASSIGN_FLOAT_DIVIDE)
    )

    fun parse(): Script {
        val decls = mutableListOf<Decl>()
        if (peek().type != TokenType.SCRIPT) {
            throw CompilerError.at(peek().position, "Script must start with a 'script' declaration.")
        }
        val scriptDecl = parseScriptDecl()
        features = ParserFeatures.forGame(scriptDecl.game)
        while (!atEnd) {
            try {
                decls.add(parseDecl())
            } catch (error: CompilerError) {
                errorLog.addError(error)
                synchronizeToDecl()
            }
        }

        if (errorLog.isFailedRun) {
            throw AggregatedCompilerError(errorLog)
        }

        return Script(decls, scriptDecl.game, scriptDecl.scriptType)
    }

    private fun parseDecl(): Decl {
        // This is just a wrapper currently. But it could turn into something
        // more complicated if we add constants or other non-code decls
        return parseEventOrFunctionDecl()
    }

    private fun parseScriptDecl(): ScriptDecl {
        consume(TokenType.SCRIPT)
        if (hasScriptDecl) {
            throw CompilerError.at(previous.position, "Only one 'script' statement is allowed.")
        }
        consumeAll(TokenType.LPAREN, TokenType.STRING)
        val gameString = previousValue.stringValue()
        val game: Game
        try {
            game = Game.valueOf(gameString)
        } catch (_: Exception) {
            throw CompilerError.at(previous.position, "Expected one of ${Game.values()}")
        }
        consumeAll(TokenType.COMMA, TokenType.INT)
        val scriptType = previousValue.intValue()
        consumeAll(TokenType.RPAREN, TokenType.SEMICOLON)
        hasScriptDecl = true
        return ScriptDecl(game, scriptType)
    }

    private fun parseEventOrFunctionDecl(): Decl {
        val annotations = parseAnnotations()
        return when (peek().type) {
            TokenType.EVENT -> parseEventDecl(annotations)
            TokenType.FUNC -> parseFunctionDecl(annotations)
            else -> throw CompilerError.at(peek().position, "Expected declaration.")
        }
    }

    private fun parseFunctionDecl(annotations: MutableList<Annotation>): FunctionDecl {
        consumeAll(TokenType.FUNC, TokenType.IDENTIFIER)
        val identifier = previous.identifier!!
        consume(TokenType.LPAREN)
        val parameters = mutableListOf<VarSymbol>()
        val body: Block
        val symbol: FunctionSymbol
        symbolTable.openNewEnvironment()
        try {
            if (!check(TokenType.RPAREN)) {
                do {
                    consume(TokenType.IDENTIFIER)
                    val parameterName = previous.identifier!!
                    val varSymbol = VarSymbol(parameterName, false)
                    symbolTable.define(varSymbol)
                    parameters.add(varSymbol)
                } while (match(TokenType.COMMA))
            }
            consume(TokenType.RPAREN)

            val testSymbol = symbolTable.lookupOrNull(identifier)
            if (testSymbol != null && testSymbol !is FunctionSymbol) {
                throw SymbolRedefinitionException(identifier)
            } else if (testSymbol != null) {
                symbol = testSymbol as FunctionSymbol
            } else {
                symbol = FunctionSymbol(identifier, parameters.size)
                symbolTable.defineTopLevel(symbol)
            }

            body = parseBlock()
        } finally {
            symbolTable.closeEnvironment()
        }

        return FunctionDecl(symbol, parameters, body, annotations)
    }

    private fun parseEventDecl(annotations: MutableList<Annotation>): EventDecl {
        consumeAll(TokenType.EVENT, TokenType.LBRACKET, TokenType.INT)
        val eventType = previousValue.intValue()
        consumeAll(TokenType.RBRACKET, TokenType.LPAREN)
        val args = mutableListOf<Literal>()
        if (!check(TokenType.RPAREN)) {
            val realSimplifyNegativeIntsValue = features.simplifyNegativeInts
            features.simplifyNegativeInts = true
            do {
                val expr = parseExpr() as? Literal ?: throw CompilerError.at(previous.position, "Expected constant.")
                args.add(expr)
            } while (match(TokenType.COMMA))
            features.simplifyNegativeInts = realSimplifyNegativeIntsValue
        }
        consume(TokenType.RPAREN)
        val body = parseBlock()
        return EventDecl(eventType, args, body, annotations)
    }

    private fun parseAnnotations(): MutableList<Annotation> {
        val annotations = mutableListOf<Annotation>()
        while (peek().type == TokenType.AT_SIGN) {
            annotations.add(parseAnnotation())
        }
        return annotations
    }

    private fun parseAnnotation(): Annotation {
        consumeAll(TokenType.AT_SIGN, TokenType.IDENTIFIER)
        val identifier = previous.identifier!!
        val args = mutableListOf<String>()
        if (match(TokenType.LPAREN)) {
            do {
                consume(TokenType.STRING)
                args.add(previousValue.stringValue())
            } while (match(TokenType.COMMA))
            consume(TokenType.RPAREN)
        }
        return Annotation(identifier, args)
    }

    private fun parseStmt(): Stmt {
        // TODO: Assess whether or not we should have a separate "concrete" statement rule
        //       so that you can't do:
        //       if (someCondition())
        //           var tmp = 1;
        return when (peek().type) {
            TokenType.FOR -> parseFor()
            TokenType.GOTO -> parseGoto()
            TokenType.IF -> parseIf()
            TokenType.LABEL -> parseLabel()
            TokenType.LBRACE -> parseBlock()
            TokenType.MATCH -> parseMatch()
            TokenType.RETURN -> parseReturn()
            TokenType.VAR -> parseVarDecl()
            TokenType.WHILE -> parseWhile()
            TokenType.YIELD -> parseYield()
            else -> parseExprStmt()
        }
    }

    private fun parseBlock(): Block {
        symbolTable.openNewEnvironment()
        val contents = mutableListOf<Stmt>()
        try {
            consume(TokenType.LBRACE)
            while (!match(TokenType.RBRACE)) {
                try {
                    contents.add(parseStmt())
                } catch (error: CompilerError) {
                    errorLog.addError(error)
                    synchronizeToStmt()
                }
            }
        } finally {
            symbolTable.closeEnvironment()
        }
        return Block(contents)
    }

    private fun parseFor(): For {
        symbolTable.openNewEnvironment()
        try {
            consumeAll(TokenType.FOR, TokenType.LPAREN)
            val init = parseExpr() as? Assignment
                ?: throw CompilerError.at(previous.position, "Expected assignment")
            if (init.op != Operator.ASSIGN) {
                throw CompilerError.at(previous.position, "Expected plain assignment")
            }
            consume(TokenType.SEMICOLON)
            val check = parseExpr()
            consume(TokenType.SEMICOLON)
            val step = ExprStmt(parseExpr())
            consume(TokenType.RPAREN)
            val body = parseStmt()
            return For(init, check, step, body)
        } finally {
            symbolTable.closeEnvironment()
        }
    }

    private fun parseGoto(): Goto {
        consumeAll(TokenType.GOTO, TokenType.IDENTIFIER)
        val identifier = previous.identifier!!
        consume(TokenType.SEMICOLON)
        val goto = Goto(null)
        labelTracker.addGoto(goto, identifier)
        return goto
    }

    private fun parseIf(): If {
        consumeAll(TokenType.IF, TokenType.LPAREN)
        val condition = parseExpr()
        consume(TokenType.RPAREN)
        val thenPart = parseStmt()
        val elsePart = if (match(TokenType.ELSE)) parseStmt() else null
        return If(condition, thenPart, elsePart)
    }

    private fun parseLabel(): Label {
        consumeAll(TokenType.LABEL, TokenType.IDENTIFIER)
        val identifier = previous.identifier!!
        consume(TokenType.SEMICOLON)
        val label = Label(LabelSymbol(identifier))
        symbolTable.defineInFunctionScope(label.symbol)
        labelTracker.addLabel(label)
        return label
    }

    private fun parseMatch(): Match {
        consumeAll(TokenType.MATCH, TokenType.LPAREN)
        val condition = parseExpr()
        consumeAll(TokenType.RPAREN, TokenType.LBRACE)
        val cases = mutableListOf<Case>()
        var default: Stmt? = null
        while (!match(TokenType.RBRACE)) {
            if (match(TokenType.ELSE)) {
                if (default != null) {
                    throw CompilerError.at(
                        previous.position,
                        "Match statement may have only one default case."
                    )
                }
                consume(TokenType.ARROW)
                default = parseStmt()
            } else {
                val expr = parseExpr()
                consume(TokenType.ARROW)
                val body = parseStmt()
                cases.add(Case(expr, body))
            }
        }
        return Match(condition, cases, default)
    }

    private fun parseReturn(): Return {
        consume(TokenType.RETURN)
        val value = if (match(TokenType.SEMICOLON)) {
            Literal.ofInt(0)
        } else {
            val expr = parseExpr()
            consume(TokenType.SEMICOLON)
            expr
        }
        return Return(value)
    }

    private fun parseWhile(): While {
        consumeAll(TokenType.WHILE, TokenType.LPAREN)
        val condition = parseExpr()
        consume(TokenType.RPAREN)
        val body = parseStmt()
        return While(condition, body)
    }

    private fun parseVarDecl(): Stmt {
        consumeAll(TokenType.VAR, TokenType.IDENTIFIER)
        val identifier = previous.identifier!!
        val symbol = VarSymbol(identifier, false)
        symbolTable.define(symbol)
        consume(TokenType.ASSIGN)
        return when (peek().type) {
            TokenType.ARRAY -> parseEmptyArrayDecl(symbol)
            TokenType.LBRACKET -> parseArrayStaticInitializer(symbol)
            else -> parseNormalVarDecl(symbol)
        }
    }

    private fun parseEmptyArrayDecl(symbol: VarSymbol): Block {
        consumeAll(TokenType.ARRAY, TokenType.LBRACKET, TokenType.INT)
        val count = previousValue.intValue()
        consumeAll(TokenType.RBRACKET, TokenType.SEMICOLON)
        val contents = mutableListOf<Stmt>()
        for (i in 0 until count) {
            symbolTable.define(symbol)
            val ref = ArrayRef(symbol, Literal.ofInt(i))
            contents.add(ExprStmt(Assignment(ref, Literal.ofInt(0), Operator.ASSIGN)))
        }
        return Block(contents)
    }

    private fun parseArrayStaticInitializer(symbol: VarSymbol): Block {
        consume(TokenType.LBRACKET)
        val contents = mutableListOf<Stmt>()
        if (!check(TokenType.RBRACKET)) {
            do {
                val ref = ArrayRef(symbol, Literal.ofInt(contents.size))
                val expr = parseExpr()
                contents.add(ExprStmt(Assignment(ref, expr, Operator.ASSIGN)))
            } while (match(TokenType.COMMA))
        }
        consumeAll(TokenType.RBRACKET, TokenType.SEMICOLON)
        return Block(contents)
    }

    private fun parseNormalVarDecl(symbol: VarSymbol): ExprStmt {
        val rhs = parseExpr()
        consume(TokenType.SEMICOLON)
        return ExprStmt(Assignment(VarRef(symbol), rhs, Operator.ASSIGN))
    }

    private fun parseYield(): Yield {
        consumeAll(TokenType.YIELD, TokenType.SEMICOLON)
        return Yield()
    }

    private fun parseExprStmt(): ExprStmt {
        val expr = parseExpr()
        consume(TokenType.SEMICOLON)
        return ExprStmt(expr)
    }

    internal fun parseExpr(precedence: Int = Operator.NO_PRECEDENCE): Expr {
        val token = advance()
        val prefixAction = prefixActions[token.type] ?: throw CompilerError.at(
            token.position,
            "Expected expression.",
        )
        var expr = prefixAction.parse(this)
        while (precedence < precedenceOfNext()) {
            val next = advance()
            val infixAction = infixActions[next.type]!!
            expr = infixAction.parse(expr, this)
        }
        return expr
    }

    private fun precedenceOfNext(): Int {
        if (infixActions.containsKey(peek().type))
            return infixActions[peek().type]!!.precedence()
        return Operator.NO_PRECEDENCE
    }

    internal fun consume(type: TokenType) {
        if (!match(type)) {
            // Provide a more precise error message for semicolons.
            if (type == TokenType.SEMICOLON)
                throw CompilerError.at(previous.position, "Expected semicolon after statement.")

            // General case. Show expected token and what we actually found.
            val actualTok = peek()
            val actual = peek().type.toString()
            val expected = type.toString()
            throw CompilerError.at(actualTok.position, "Expected \"$expected\", found \"$actual\".")
        }
    }

    private fun synchronizeToDecl() {
        while (!atEnd) {
            if (peek().type in declBeginnings)
                return
            advance()
        }
    }

    private fun synchronizeToStmt() {
        while (!atEnd) {
            when (peek().type) {
                in stmtBeginnings -> {
                    return
                }
                TokenType.SEMICOLON -> {
                    advance()
                    return
                }
                else -> {
                    advance()
                }
            }

        }
    }

    private fun consumeAll(vararg types: TokenType) {
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

    internal val previous: Token
        get() = tokens[position - 1]

    internal val previousValue: Literal
        get() = previous.value!!

    private fun advance(): Token {
        assert(position < tokens.size)
        return tokens[position++]
    }

    private fun peek(): Token {
        if (atEnd)
            throw CompilerError.at(tokens.last().position, "Reached EOF while parsing.")
        return tokens[position]
    }
}