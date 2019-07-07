package decompiler

import ast.*
import common.Format3DS
import common.Opcode3DS
import common.TokenType
import common.TranslationEngine
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

private data class EventHeader(
    val bodyAddress: Int,
    val type: Int,
    val arity: Int,
    val id: Int,
    val name: String,
    val args: List<Literal>
)

private enum class ArgType {
    INT, STRING
}

private enum class ExprState {
    NORMAL, SHORTHAND_ASSIGNMENT
}

class Decompiler3DS private constructor(private val input: ByteArray, private val enableExperimental: Boolean) {
    private var position = 0
    private val headers = mutableListOf<EventHeader>()
    private lateinit var textData: Map<Int, String>

    private val definedVars = hashMapOf<Int, VarSymbol>()
    private val unresolvedGotos = hashMapOf<Int, MutableList<Goto>>()
    private val exprStack = ArrayDeque<Expr>()
    private var exprState = ExprState.NORMAL

    companion object {
        private const val VAR_BASE_NAME = "v"

        private val SIGNATURES = hashMapOf(
            0x10 to listOf(ArgType.INT, ArgType.INT, ArgType.INT),
            0x11 to listOf(ArgType.INT, ArgType.INT, ArgType.INT),
            0x12 to listOf(ArgType.INT, ArgType.INT, ArgType.INT),
            0x13 to listOf(ArgType.INT, ArgType.INT, ArgType.INT),
            0x14 to listOf(ArgType.INT, ArgType.INT, ArgType.INT, ArgType.INT, ArgType.INT, ArgType.STRING),
            0x15 to listOf(
                ArgType.INT, ArgType.INT, ArgType.INT,
                ArgType.INT, ArgType.INT, ArgType.INT,
                ArgType.INT, ArgType.STRING
            ),
            0x16 to listOf(
                ArgType.INT, ArgType.INT, ArgType.INT, ArgType.INT,
                ArgType.INT, ArgType.INT, ArgType.INT, ArgType.INT,
                ArgType.INT, ArgType.INT, ArgType.INT, ArgType.INT, ArgType.INT
            ),
            0x17 to listOf(ArgType.STRING, ArgType.INT, ArgType.STRING, ArgType.INT, ArgType.INT, ArgType.STRING),
            0x18 to listOf(ArgType.STRING, ArgType.INT, ArgType.STRING, ArgType.INT, ArgType.INT, ArgType.STRING),
            0x19 to listOf(ArgType.STRING, ArgType.INT, ArgType.INT, ArgType.INT, ArgType.INT, ArgType.STRING),
            0x1B to listOf(ArgType.STRING, ArgType.INT, ArgType.STRING, ArgType.INT),
            0x1C to listOf(ArgType.STRING, ArgType.INT),
            0x1D to listOf(ArgType.STRING, ArgType.INT, ArgType.STRING),
            0x1E to listOf(ArgType.STRING),
            0x1F to listOf(ArgType.STRING),
            0x20 to listOf(ArgType.STRING, ArgType.INT)
        )

        private val SHORTHAND_TOKEN_TYPES = hashMapOf(
            Opcode3DS.PLUS to TokenType.ASSIGN_PLUS,
            Opcode3DS.MINUS to TokenType.ASSIGN_MINUS,
            Opcode3DS.TIMES to TokenType.ASSIGN_TIMES,
            Opcode3DS.DIVIDE to TokenType.ASSIGN_DIVIDE,
            Opcode3DS.FPLUS to TokenType.ASSIGN_FPLUS,
            Opcode3DS.FMINUS to TokenType.ASSIGN_FMINUS,
            Opcode3DS.FTIMES to TokenType.ASSIGN_FMINUS,
            Opcode3DS.FDIVIDE to TokenType.ASSIGN_FDIVIDE
        )

        fun decompile(input: ByteArray, enableExperimental: Boolean): Block {
            val decompiler = Decompiler3DS(input, enableExperimental)
            return decompiler.decompile()
        }
    }

    fun decompile(): Block {
        verifyViableSize()
        verifyMagic()

        seek(Format3DS.EVENT_TABLE_POINTER)
        val eventTableAddress = nextInt()
        val textDataAddress = nextInt()
        val eventTable = readEventTable(eventTableAddress)
        textData = readTextData(textDataAddress)

        val events = mutableListOf<Stmt>()
        for (eventAddress in eventTable)
            headers.add(decompileEventHeader(eventAddress))
        for (header in headers)
            events.add(decompileEvent(header))
        return Block(events)
    }

    private fun verifyViableSize() {
        if (input.size < Format3DS.TEXT_DATA_POINTER)
            throw DecompileError("File is too small to be a valid CMB", -1)
    }

    private fun verifyMagic() {
        if (String(input.sliceArray(0..2), Format3DS.ENCODING) != "cmb")
            throw DecompileError("Not a valid CMB: Incorrect magic number", 0)
    }

    private fun readEventTable(start: Int): List<Int> {
        seek(start)
        val result = mutableListOf<Int>()
        var done = false
        while (!done) {
            val eventAddress = nextInt()
            if (eventAddress == 0)
                done = true
            else
                result.add(eventAddress)
        }
        return result
    }

    private fun readTextData(start: Int): Map<Int, String> {
        val result = hashMapOf<Int, String>()
        seek(start)
        while (position < input.size)
            result[position - start] = nextString()
        return result
    }

    private fun decompileEventHeader(start: Int): EventHeader {
        seek(start)
        if (nextInt() != start)
            throw DecompileError("Corrupted header", position)
        val bodyAddress = nextInt()
        val type = next()
        val arity = next()
        seek(position + 2) // Don't need frame size or padding.
        val id = nextInt()
        val name = readEventName(id)
        val args = readEventArgs(nextInt(), type.toInt(), arity.toInt())
        return EventHeader(
            bodyAddress, type.toInt(), arity.toInt(),
            id, name, args
        )
    }

    private fun readEventName(id: Int): String {
        val address = nextInt()
        return if (address == 0)
            "fn$id"
        else {
            val endAddress = position
            seek(address)
            val result = nextString()
            seek(endAddress)
            result
        }
    }

    private fun readEventArgs(address: Int, type: Int, arity: Int): List<Literal> {
        seek(address)
        val result = mutableListOf<Literal>()
        if (type == 0)
            return result

        val signature = SIGNATURES[type]
        val unrecognized = arity > 0 && (signature == null || arity != signature.size)
        if (unrecognized)
            println("Warning: unrecognized event type: $type. Event arguments may be incorrect!")
        for (i in 0 until arity) {
            val value = nextInt()
            if (unrecognized) {
                if (value in textData)
                    result.add(Literal(textData[value] ?: error("String argument isn't in text data")))
                else
                    result.add(Literal(value))
            } else {
                when (signature!![i]) {
                    ArgType.INT -> result.add(Literal(value))
                    ArgType.STRING -> {
                        val str = textData[value] ?: error("String argument isn't in text data")
                        result.add(Literal(str))
                    }
                }
            }
        }
        return result
    }

    private fun decompileEvent(header: EventHeader): AbstractEventDecl {
        definedVars.clear()
        if (header.type == 0)
            defineFunctionParameters(header.arity)
        unresolvedGotos.clear()
        exprStack.clear()
        exprState = ExprState.NORMAL

        seek(header.bodyAddress)
        val block = decompileBlock()
        block.accept(GotoResolver(unresolvedGotos))
        if (enableExperimental) {
            block.accept(WhileFolder())
            block.accept(ForFolder())
        }
        if (lastIsEmptyReturn(block))
            block.contents.removeAt(block.contents.lastIndex)
        // TODO: numVars
        val symbol = EventSymbol(header.name, header.id, header.type, header.arity, -1)

        return if (header.type == 0)
            FuncDecl(symbol, block, functionParametersToList(header.arity))
        else
            EventDecl(symbol, block, header.args)
    }

    private fun defineFunctionParameters(numParams: Int) {
        for (i in 0 until numParams)
            definedVars[i] = VarSymbol(VAR_BASE_NAME + i, i)
    }

    private fun functionParametersToList(numParams: Int): List<VarSymbol> {
        val result = mutableListOf<VarSymbol>()
        for (i in 0 until numParams)
            result.add(definedVars[i]!!)
        return result
    }

    private fun lastIsEmptyReturn(block: Block): Boolean {
        if (block.contents.isNotEmpty()) {
            val last = block.contents.last()
            if (last is Return && last.value == null)
                return true
        }
        return false
    }

    private fun decompileBlock(length: Int = -1): Block {
        val stop = if (length == -1) Integer.MAX_VALUE else position + length
        val contents = mutableListOf<Stmt>()
        var nextStatementAddress = position
        while (position < stop && peek().toInt() != 0) {
            val prevNumStmts = contents.size
            when (val op = Opcode3DS.findByValue(next())) {
                Opcode3DS.VAR_GET -> exprStack.push(decompileVarRef(false))
                Opcode3DS.ARR_GET -> exprStack.push(decompileArrayRef(false))
                Opcode3DS.PTR_GET -> exprStack.push(decompilePtr(false))
                Opcode3DS.VAR_LOAD -> exprStack.push(decompileVarRef(true))
                Opcode3DS.ARR_LOAD -> exprStack.push(decompileArrayRef(true))
                Opcode3DS.PTR_LOAD -> exprStack.push(decompilePtr(true))
                Opcode3DS.LOAD_BYTE -> exprStack.push(Literal(next().toInt()))
                Opcode3DS.LOAD_SHORT -> exprStack.push(Literal(nextBigEndian(Short.SIZE_BYTES)))
                Opcode3DS.LOAD_INT -> exprStack.push(Literal(nextBigEndian(Int.SIZE_BYTES)))
                Opcode3DS.LOAD_TEXT_BYTE -> exprStack.push(decompileTextRef(Byte.SIZE_BYTES))
                Opcode3DS.LOAD_TEXT_SHORT -> exprStack.push(decompileTextRef(Short.SIZE_BYTES))
                Opcode3DS.LOAD_TEXT_INT -> exprStack.push(decompileTextRef(Int.SIZE_BYTES))
                Opcode3DS.LOAD_FLOAT -> exprStack.push(decompileFloat())
                Opcode3DS.COPY_AND_DEREFERENCE_TOP -> exprState = ExprState.SHORTHAND_ASSIGNMENT
                Opcode3DS.CONSUME_TOP -> contents.add(ExprStmt(popExpr()))
                Opcode3DS.COMPLETE_ASSIGN -> contents.add(decompileAssignment())
                Opcode3DS.FIX, Opcode3DS.FLOAT, Opcode3DS.NEGATE_INT,
                Opcode3DS.NEGATE_REAL, Opcode3DS.BNOT, Opcode3DS.NOT -> exprStack.push(decompileUnaryExpr(op))
                Opcode3DS.PLUS, Opcode3DS.FPLUS, Opcode3DS.MINUS,
                Opcode3DS.FMINUS, Opcode3DS.TIMES, Opcode3DS.FTIMES,
                Opcode3DS.DIVIDE, Opcode3DS.FDIVIDE, Opcode3DS.MODULO,
                Opcode3DS.BOR, Opcode3DS.BAND, Opcode3DS.XOR,
                Opcode3DS.LS, Opcode3DS.RS, Opcode3DS.EQ,
                Opcode3DS.FEQ, Opcode3DS.NE, Opcode3DS.FNE,
                Opcode3DS.LT, Opcode3DS.FLT, Opcode3DS.LE,
                Opcode3DS.FLE, Opcode3DS.GT, Opcode3DS.FGT,
                Opcode3DS.GE, Opcode3DS.FGE -> {
                    if (exprState == ExprState.SHORTHAND_ASSIGNMENT && Opcode3DS.findByValue(peek()) == Opcode3DS.COMPLETE_ASSIGN)
                        contents.add(decompileShorthandAssignment(op))
                    else
                        exprStack.push(decompileBinaryExpr(op))
                }
                Opcode3DS.LOCAL_CALL -> exprStack.push(decompileLocalCall())
                Opcode3DS.GLOBAL_CALL -> exprStack.push(decompileGlobalCall())
                Opcode3DS.SET_RETURN -> contents.add(Return(popExpr()))
                Opcode3DS.JUMP -> contents.add(decompileGoto())
                Opcode3DS.OR, Opcode3DS.AND -> exprStack.push(decompileShortCircuitedExpr(op))
                Opcode3DS.JUMP_ZERO -> contents.add(decompileIf())
                Opcode3DS.YIELD -> contents.add(Yield())
                Opcode3DS.COPY_TOP -> contents.add(decompileMatch())
                Opcode3DS.FORMAT -> contents.add(decompileFormat())
                Opcode3DS.INC, Opcode3DS.DEC -> exprStack.push(decompileIncrement(op))
                Opcode3DS.RETURN_FALSE -> contents.add(Return())
                Opcode3DS.RETURN_TRUE -> contents.add(Return(Literal(1)))
                else -> throw DecompileError("Encountered ${op.name} in unexpected position", position)
            }
            if (contents.size > prevNumStmts) {
                contents.last().address = nextStatementAddress
                nextStatementAddress = position
            }
        }
        return Block(contents)
    }

    private fun decompileFormat(): ExprStmt {
        val numArgs = next().toInt()
        val args = mutableListOf<Expr>()
        for (i in 0 until numArgs)
            args.add(exprStack.pop())
        args.reverse()
        return ExprStmt(Funcall("format", args))
    }

    private fun decompileTextRef(offsetSize: Int): Literal {
        return Literal(
            textData[nextBigEndian(offsetSize)] ?: throw DecompileError("Bad text reference", position)
        )
    }

    private fun decompileFloat(): Literal {
        if (input.size < position + Int.SIZE_BYTES)
            throw DecompileError("Reached EOF while decompiling", position)

        val buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
        buffer.put(input.sliceArray(position until position + Int.SIZE_BYTES))
        position += Int.SIZE_BYTES
        return Literal(buffer.getFloat(0))
    }

    private fun decompileVarRef(isPointer: Boolean): VarRef {
        val frameIndex = next().toInt()
        val symbol = if (frameIndex in definedVars) {
            definedVars[frameIndex]
        } else {
            val symbol = VarSymbol("", frameIndex)
            definedVars[frameIndex] = symbol
            symbol
        }
        return VarRef(symbol!!, isPointer)
    }

    private fun decompileArrayRef(isPointer: Boolean): ArrayRef {
        return ArrayRef(decompileVarRef(isPointer).symbol, popExpr(), isPointer)
    }

    private fun decompilePtr(isPointer: Boolean): ArrayRef {
        val ref = decompileArrayRef(isPointer)
        ref.symbol.isExternal = true
        return ref
    }

    private fun decompileUnaryExpr(op: Opcode3DS): UnaryExpr {
        return UnaryExpr(op.toTokenType(), popExpr())
    }

    private fun decompileIncrement(op: Opcode3DS): Increment {
        val ref = popExpr() as? Ref ?: throw DecompileError("Expected reference", position)
        ref.isPointer = false
        val isPrefix = isPrefix()
        if (isPrefix)
            position += 2 // Skip var load
        else
            popExpr() // Throw out the postfix expression's return value
        return Increment(op.toTokenType(), ref, isPrefix)
    }

    private fun isPrefix(): Boolean {
        return peek() == Opcode3DS.VAR_GET.opcode
    }

    private fun decompileBinaryExpr(op: Opcode3DS): BinaryExpr {
        val rhs = popExpr()
        return BinaryExpr(popExpr(), op.toTokenType(), rhs)
    }

    private fun decompileShortCircuitedExpr(op: Opcode3DS): BinaryExpr {
        val lhs = popExpr()
        val block = decompileBlock(nextBigEndian(Short.SIZE_BYTES) - 2)
        assert(block.contents.isEmpty())
        return BinaryExpr(lhs, op.toTokenType(), popExpr())
    }

    private fun decompileLocalCall(): Funcall {
        val id = next().toInt()
        if (id >= headers.size)
            throw DecompileError("Local call references non-existent event", position)
        return Funcall(TranslationEngine.toEnglish(headers[id].name), popFunctionArgs(headers[id].arity), id)
    }

    private fun popFunctionArgs(numArgs: Int): List<Expr> {
        val args = mutableListOf<Expr>()
        for (i in 0 until numArgs)
            args.add(popExpr())
        args.reverse()
        return args
    }

    private fun decompileGlobalCall(): Funcall {
        val name = textData[nextBigEndian(Short.SIZE_BYTES)]
            ?: throw DecompileError("Global call requests out-of-bounds text", position)
        return Funcall(TranslationEngine.toEnglish(name), popFunctionArgs(next().toInt()))
    }

    private fun decompileShorthandAssignment(op: Opcode3DS): ExprStmt {
        position += 1 // Consume the complete assignment op.
        val rhs = popExpr()
        val lhs = getAssignmentLeftHandSide()
        val type =
            SHORTHAND_TOKEN_TYPES[op] ?: throw DecompileError("Unexpected operator in shorthand assignment", position)
        exprState = ExprState.NORMAL
        return ExprStmt(BinaryExpr(lhs, type, rhs))
    }

    private fun getAssignmentLeftHandSide(): Ref {
        val lhs = popExpr() as? Ref
            ?: throw DecompileError("Left hand side of assignment isn't a reference", position)
        lhs.isPointer = false
        return lhs
    }

    private fun decompileAssignment(): ExprStmt {
        val rhs = popExpr()
        return ExprStmt(BinaryExpr(getAssignmentLeftHandSide(), TokenType.ASSIGN, rhs))
    }

    private fun decompileGoto(): Goto {
        val dest = position + nextBigEndian(Short.SIZE_BYTES)
        val goto = Goto(null)
        val gotos = unresolvedGotos[dest]
        if (gotos == null)
            unresolvedGotos[dest] = mutableListOf(goto)
        else
            gotos.add(goto)
        return goto
    }

    private fun decompileIf(): If {
        val cond = popExpr()
        val blockSize = nextBigEndian(Short.SIZE_BYTES) - 2
        if (enableExperimental && backHasElseGoto(blockSize)) {
            val thenPart = decompileBlock(blockSize - 3)
            next() // Consume jump opcode
            val elseSize = nextBigEndian(Short.SIZE_BYTES) - 2
            val elsePart = decompileBlock(elseSize)
            return If(cond, thenPart, elsePart)
        }
        val thenPart = decompileBlock(blockSize)
        return If(cond, thenPart, null)
    }

    private fun backHasElseGoto(blockSize: Int): Boolean {
        val backIsGoto = input[position + blockSize - 3] == Opcode3DS.JUMP.opcode
        return backIsGoto && readBigEndianInt(position + blockSize - 2, 2) > 0
    }

    private fun decompileMatch(): Match {
        val switch = popExpr()
        val cases = mutableListOf<Case>()
        var default: Stmt? = null
        var stopAddress = -1
        while (stopAddress == -1 || position < stopAddress) {
            val nextOp = Opcode3DS.findByValue(peek())
            if (stopAddress == -1 || nextOp == Opcode3DS.COPY_TOP) {
                if (nextOp == Opcode3DS.COPY_TOP)
                    position += 1
                val condBody = decompileBlock(getLengthToEndOfCase())
                assert(condBody.contents.isEmpty())
                val cond = popExpr()
                position += 1

                assert(Opcode3DS.findByValue(peek()) == Opcode3DS.JUMP_NOT_ZERO)
                position += 4 // Skip to jump over block. We'll use this to get the block size.
                cases.add(Case(cond, decompileBlock(nextBigEndian(Short.SIZE_BYTES) - 5)))

                assert(Opcode3DS.findByValue(peek()) == Opcode3DS.JUMP)
                position += 1
                stopAddress = position + nextBigEndian(Short.SIZE_BYTES)
            } else {
                assert(stopAddress - position - 3 > 0)
                default = decompileBlock(stopAddress - position - 3)
                position += 3
            }
        }
        position += 1
        return Match(switch, cases, default)
    }

    private fun getLengthToEndOfCase(): Int {
        val start = position
        while (next() != Opcode3DS.JUMP_NOT_ZERO.opcode);
        val length = position - start - 2 // End before the comparison OP
        position = start
        return length
    }

    private fun popExpr(): Expr {
        if (exprStack.isEmpty())
            throw DecompileError("Expression stack unexpectedly empty", position)
        return exprStack.pop()
    }

    private fun nextString(): String {
        val start = position
        while (next() != 0.toByte());
        return String(input.sliceArray(start until position - 1), Format3DS.ENCODING)
    }

    private fun nextInt(): Int {
        if (position + 4 > input.size)
            throw DecompileError("Reached EOF while reading file", position)

        val buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(input.sliceArray(position until position + 4))
        position += 4
        return buffer.getInt(0)
    }

    private fun nextBigEndian(numBytes: Int): Int {
        val result = readBigEndianInt(position, numBytes)
        position += numBytes
        return result
    }

    private fun readBigEndianInt(start: Int, numBytes: Int): Int {
        if (position + numBytes > input.size)
            throw DecompileError("Reached EOF while decompiling", position)
        if (numBytes == 1)
            return input[start].toInt()
        assert(numBytes == Short.SIZE_BYTES || numBytes == Int.SIZE_BYTES)
        val buffer = ByteBuffer.allocate(numBytes).order(ByteOrder.BIG_ENDIAN)
        buffer.put(input.sliceArray(start until start + numBytes))
        return if (numBytes == Short.SIZE_BYTES)
            buffer.getShort(0).toInt()
        else
            buffer.getInt(0)
    }

    private fun next(): Byte {
        verifyPositionInFileBounds()
        return input[position++]
    }

    private fun peek(): Byte {
        verifyPositionInFileBounds()
        return input[position]
    }

    private fun verifyPositionInFileBounds() {
        if (position < 0 || position > input.size)
            throw DecompileError("Reached EOF while decompiling", position)
    }

    private fun seek(address: Int) {
        if (address < 0 || address > input.size)
            throw DecompileError("Cannot seek out of file bounds", position)
        position = address
    }
}