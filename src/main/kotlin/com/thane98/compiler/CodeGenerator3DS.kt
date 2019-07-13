package com.thane98.compiler

import com.thane98.ast.*
import com.thane98.common.*
import java.io.File
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.ByteOrder

private data class JumpMarker(val address: Int, val symbol: LabelSymbol)

class CodeGenerator3DS private constructor(private val script: Block, private val outName: String) : ExprVisitor<Unit>,
    StmtVisitor<Unit> {
    private val result = mutableListOf<Byte>()
    private val jumpMarkers = mutableListOf<JumpMarker>()
    private val rawText = mutableListOf<Byte>()
    private val textMap = hashMapOf<String, Int>()

    companion object {
        private val HEADER_BASE = mutableListOf<Byte>(
            0x63, 0x6D, 0x62, 0x00, 0x19, 0x08, 0x11, 0x20,
            0x00, 0x00, 0x00, 0x00, 0x28, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        )

        private val BYTE_RANGE = Byte.MIN_VALUE..Byte.MAX_VALUE
        private val SHORT_RANGE = Short.MIN_VALUE..Short.MAX_VALUE

        fun generate(script: Block, outName: String): List<Byte> {
            val generator = CodeGenerator3DS(script, File(outName).name)
            return generator.generate()
        }
    }

    fun generate(): List<Byte> {
        result.addAll(HEADER_BASE)
        result.addString(outName)
        pad(result)
        val eventTableAddress = result.size
        result.overwrite(Format3DS.EVENT_TABLE_POINTER, eventTableAddress)

        // Reserve space for pointers to every event + a null terminator
        val numEvents = script.contents.size
        for (i in 0 until (numEvents + 1) * 4)
            result.add(0)
        for (i in 0 until numEvents) {
            pad(result)
            result.overwrite(eventTableAddress + i * 4, result.size)
            script.contents[i].accept(this)
        }
        result.overwrite(Format3DS.TEXT_DATA_POINTER, result.size)
        result.addAll(rawText)
        pad(result)
        return result
    }

    private fun pad(buffer: MutableList<Byte>) {
        while (buffer.size % 4 != 0)
            buffer.add(0)
    }

    private fun MutableList<Byte>.overwrite(index: Int, value: Int) {
        val raw = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value)
        for (i in 0 until 4)
            set(index + i, raw[i])
    }

    private fun MutableList<Byte>.overwriteBigEndianShort(index: Int, value: Int) {
        assert(value in SHORT_RANGE)
        val raw = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(value)
        for (i in 0 until 2)
            set(index + i, raw[i + 2])
    }

    private fun MutableList<Byte>.addInt(value: Int) {
        val raw = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value)
        for (i in 0 until 4)
            add(raw[i])
    }

    private fun MutableList<Byte>.addFloat(value: Float, order: ByteOrder = ByteOrder.BIG_ENDIAN) {
        val raw = ByteBuffer.allocate(4).order(order).putFloat(value)
        for (i in 0 until 4)
            add(raw[i])
    }

    private fun MutableList<Byte>.addBigEndian(value: Int, numBytes: Int) {
        assert(numBytes in 1..4)
        val raw = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(value)
        for (i in 4 - numBytes until 4)
            add(raw[i])
    }

    private fun MutableList<Byte>.add(op: Opcode3DS) {
        add(op.opcode)
    }

    private fun MutableList<Byte>.add(value: Int) {
        add(value.toByte())
    }

    private fun MutableList<Byte>.addString(string: String) {
        // Some shift-jis implementations fail to encode certain characters correctly.
        // To fix this, we replace them with characters that will encode correctly.
        val sb = StringBuilder(string)
        for (i in 0 until sb.length) {
            if (sb[i] == '\uff0d')
                sb[i] = '\u2212'
            else if (sb[i] == '\uff5e')
                sb[i] = '\u301c'
        }

        val toAdd = sb.toString()
        for (byte in toAdd.toByteArray(Format3DS.ENCODING))
            add(byte)
        add(0)
    }

    private fun tryAddText(text: String): Int {
        if (!textMap.containsKey(text)) {
            val offset = rawText.size
            textMap[text] = rawText.size
            rawText.addString(text)
            return offset
        }
        return textMap[text]!!
    }

    private fun createJumpMarker(op: Opcode3DS, label: LabelSymbol) {
        result.add(op)
        jumpMarkers.add(JumpMarker(result.size, label))
        result.add(0)
        result.add(0)
    }

    override fun visitLiteral(expr: Literal) {
        when (expr.value) {
            is Int -> writeVariableLengthInt(
                expr.value,
                Opcode3DS.LOAD_BYTE,
                Opcode3DS.LOAD_SHORT,
                Opcode3DS.LOAD_INT
            )
            is Float -> writeFloat(expr.value)
            is String -> writeVariableLengthInt(
                tryAddText(expr.value),
                Opcode3DS.LOAD_TEXT_BYTE,
                Opcode3DS.LOAD_TEXT_SHORT,
                Opcode3DS.LOAD_TEXT_INT
            )
            else -> assert(false) { "Unexpected value in literal." }
        }
    }

    private fun writeFloat(value: Float) {
        result.add(Opcode3DS.LOAD_FLOAT)
        result.addFloat(value)
    }

    private fun writeVariableLengthInt(value: Int, byteOp: Opcode3DS, shortOp: Opcode3DS, elseOp: Opcode3DS) {
        when (value) {
            in BYTE_RANGE -> {
                result.add(byteOp)
                result.add(value)
            }
            in SHORT_RANGE -> {
                result.add(shortOp)
                result.addBigEndian(value, Short.SIZE_BYTES)
            }
            else -> {
                result.add(elseOp)
                result.addBigEndian(value, Int.SIZE_BYTES)
            }
        }
    }

    override fun visitUnaryExpr(expr: UnaryExpr) {
        expr.expr.accept(this)
        when {
            expr.op == TokenType.MINUS -> result.add(Opcode3DS.NEGATE_INT)
            expr.op == TokenType.FMINUS -> result.add(Opcode3DS.NEGATE_REAL)
            else -> result.add(expr.op.toOpcode3DS())
        }
    }

    override fun visitIncrement(expr: Increment) {
        if (expr.isPrefix) {
            expr.target.isPointer = true
            expr.target.accept(this)
            expr.target.isPointer = false
            result.add(expr.op.toOpcode3DS())
            expr.target.accept(this)
        } else {
            expr.target.accept(this)
            expr.target.isPointer = true
            expr.target.accept(this)
            expr.target.isPointer = false
            result.add(expr.op.toOpcode3DS())
        }
    }

    override fun visitBinaryExpr(expr: BinaryExpr) {
        if (expr.op.isAssignment())
            writeAssignment(expr)
        else if (expr.op == TokenType.AND || expr.op == TokenType.OR)
            writeShortCircuitedExpression(expr)
        else {
            expr.lhs.accept(this)
            expr.rhs.accept(this)
            result.add(expr.op.toOpcode3DS())
        }
    }

    private fun writeShortCircuitedExpression(expr: BinaryExpr) {
        expr.lhs.accept(this)
        val endLabel = LabelSymbol("")
        createJumpMarker(expr.op.toOpcode3DS(), endLabel)
        expr.rhs.accept(this)
        endLabel.address = result.size
    }

    private fun writeAssignment(expr: BinaryExpr) {
        // Write left hand side.
        val ref = expr.lhs as Ref
        ref.isPointer = true
        ref.accept(this)
        ref.isPointer = false

        // Write right hand side. Structure is different if not a basic assignment.
        if (expr.op == TokenType.ASSIGN)
            expr.rhs.accept(this)
        else {
            result.add(Opcode3DS.COPY_AND_DEREFERENCE_TOP)
            expr.rhs.accept(this)
            result.add(expr.op.toOpcode3DS())
        }
        result.add(Opcode3DS.COMPLETE_ASSIGN)
    }

    override fun visitGroupedExpr(expr: GroupedExpr) {
        expr.expr.accept(this)
    }

    override fun visitFuncall(expr: Funcall) {
        for (arg in expr.args)
            arg.accept(this)
        when {
            expr.target == "format" -> {
                result.add(Opcode3DS.FORMAT)
                result.add(expr.args.size)
            }
            expr.isLocalCall -> {
                result.add(Opcode3DS.LOCAL_CALL)
                result.add(expr.callID)
            }
            else -> {
                result.add(Opcode3DS.GLOBAL_CALL)
                result.addBigEndian(tryAddText(TranslationEngine.toJapanese(expr.target)), Short.SIZE_BYTES)
                result.add(expr.args.size)
            }
        }
    }

    override fun visitVarRef(expr: VarRef) {
        if (expr.symbol.isExternal) {
            writePtr(expr.symbol.frameID, Literal(0), expr.isPointer)
            return
        }

        if (expr.isPointer)
            result.add(Opcode3DS.VAR_LOAD)
        else
            result.add(Opcode3DS.VAR_GET)
        result.add(expr.symbol.frameID)
    }

    override fun visitArrayRef(expr: ArrayRef) {
        if (expr.symbol.isExternal) {
            writePtr(expr.symbol.frameID, expr.index, expr.isPointer)
            return
        }

        expr.index.accept(this)
        if (expr.isPointer)
            result.add(Opcode3DS.ARR_LOAD)
        else
            result.add(Opcode3DS.ARR_GET)
        result.add(expr.symbol.frameID)
    }

    private fun writePtr(baseIndex: Int, index: Expr, isPointer: Boolean) {
        index.accept(this)
        if (isPointer)
            result.add(Opcode3DS.PTR_LOAD)
        else
            result.add(Opcode3DS.PTR_GET)
        result.add(baseIndex)
    }

    override fun visitBlock(stmt: Block) {
        for (entry in stmt.contents)
            entry.accept(this)
    }

    override fun visitFuncDecl(stmt: FuncDecl) {
        val subheader = mutableListOf<Byte>()
        if (stmt.symbol.name.contains("::"))
            subheader.addString(TranslationEngine.toJapanese(stmt.symbol.name))
        writeEventContents(stmt, subheader)
    }

    override fun visitEventDecl(stmt: EventDecl) {
        writeEventContents(stmt, writeEventArgs(stmt.args))
    }

    private fun writeEventArgs(args: List<Literal>): List<Byte> {
        val result = mutableListOf<Byte>()
        for (arg in args) {
            when (arg.value) {
                is Int -> result.addInt(arg.value)
                is Float -> result.addFloat(arg.value, ByteOrder.LITTLE_ENDIAN)
                is String -> result.addInt(tryAddText(arg.value))
                else -> assert(false) { "Unexpected type in literal." }
            }
        }
        return result
    }

    private fun writeEventContents(event: AbstractEventDecl, subheader: List<Byte>) {
        val symbol = event.symbol
        val subheaderAddress = result.size + Format3DS.EVENT_HEADER_SIZE
        val bodyAddress = subheaderAddress + subheader.size
        result.addInt(result.size)
        result.addInt(bodyAddress)
        result.add(symbol.type)
        result.add(symbol.arity)
        result.add(symbol.numVars)
        result.add(0) // Padding
        result.addInt(symbol.id)
        if (symbol.type != 0 || subheader.isEmpty())
            result.addInt(0) // events and anonymous functions have no names.
        else
            result.addInt(subheaderAddress)
        if (symbol.type == 0)
            result.addInt(0) // Null subheader for functions.
        else
            result.addInt(subheaderAddress)
        result.addAll(subheader)
        jumpMarkers.clear()
        event.contents.accept(this)
        resolveLabels()
        result.add(Opcode3DS.RETURN_FALSE)
        result.add(0)
    }

    private fun resolveLabels() {
        for (marker in jumpMarkers) {
            assert(marker.symbol.address != -1)
            val displacement = marker.symbol.address - marker.address
            result.overwriteBigEndianShort(marker.address, displacement)
        }
    }

    override fun visitIf(stmt: If) {
        stmt.cond.accept(this)
        val doneLabel = LabelSymbol("")
        val elseLabel = if (stmt.elsePart == null) null else LabelSymbol("")
        val firstJumpDest = elseLabel ?: doneLabel
        createJumpMarker(Opcode3DS.JUMP_ZERO, firstJumpDest)
        stmt.thenPart.accept(this)
        if (stmt.elsePart != null) {
            createJumpMarker(Opcode3DS.JUMP, doneLabel)
            elseLabel!!.address = result.size
            stmt.elsePart.accept(this)
        }
        doneLabel.address = result.size
    }

    override fun visitLabel(stmt: Label) {
        stmt.symbol.address = result.size
    }

    override fun visitGoto(stmt: Goto) {
        assert(stmt.target != null)
        createJumpMarker(Opcode3DS.JUMP, stmt.target!!)
    }

    override fun visitExprStmt(stmt: ExprStmt) {
        stmt.expr.accept(this)
        if (shouldConsumeTop(stmt.expr))
            result.add(Opcode3DS.CONSUME_TOP)
    }

    private fun shouldConsumeTop(expr: Expr): Boolean {
        return !(expr is BinaryExpr && expr.op.isAssignment())
                && !(expr is Funcall && expr.target == "format")
    }

    override fun visitReturn(stmt: Return) {
        if (stmt.value is Literal && stmt.value.value is Int && stmt.value.value == 1)
            result.add(Opcode3DS.RETURN_TRUE)
        else if (stmt.value != null) {
            stmt.value.accept(this)
            result.add(Opcode3DS.SET_RETURN)
        } else {
            result.add(Opcode3DS.RETURN_FALSE)
        }
    }

    override fun visitYield(stmt: Yield) {
        result.add(Opcode3DS.YIELD)
    }

    override fun visitWhile(stmt: While) {
        val checkLabel = LabelSymbol("", result.size)
        val doneLabel = LabelSymbol("")
        stmt.cond.accept(this)
        createJumpMarker(Opcode3DS.JUMP_ZERO, doneLabel)
        stmt.body.accept(this)
        createJumpMarker(Opcode3DS.JUMP, checkLabel)
        doneLabel.address = result.size
    }

    override fun visitFor(stmt: For) {
        val stepLabel = LabelSymbol("")
        val checkLabel = LabelSymbol("")
        val doneLabel = LabelSymbol("")
        stmt.init?.accept(this)
        createJumpMarker(Opcode3DS.JUMP, checkLabel)
        stepLabel.address = result.size
        stmt.step?.accept(this)
        checkLabel.address = result.size
        stmt.check.accept(this)
        createJumpMarker(Opcode3DS.JUMP_ZERO, doneLabel)
        stmt.body.accept(this)
        createJumpMarker(Opcode3DS.JUMP, stepLabel)
        doneLabel.address = result.size
    }

    override fun visitMatch(stmt: Match) {
        stmt.switch.accept(this)
        val doneLabel = LabelSymbol("")
        var nextCaseLabel: LabelSymbol? = null
        for (case in stmt.cases) {
            if (nextCaseLabel != null)
                nextCaseLabel.address = result.size

            // Write the check.
            result.add(Opcode3DS.COPY_TOP)
            case.cond.accept(this)
            result.add(Opcode3DS.EQ)

            val blockLabel = LabelSymbol("")
            nextCaseLabel = if (case == stmt.cases.last() && stmt.default == null)
                doneLabel
            else
                LabelSymbol("")
            createJumpMarker(Opcode3DS.JUMP_NOT_ZERO, blockLabel)
            createJumpMarker(Opcode3DS.JUMP, nextCaseLabel)

            blockLabel.address = result.size
            case.body.accept(this)
            createJumpMarker(Opcode3DS.JUMP, doneLabel)
        }
        if (stmt.default != null) {
            nextCaseLabel?.address = result.size
            stmt.default.accept(this)
            createJumpMarker(Opcode3DS.JUMP, doneLabel)
        }
        doneLabel.address = result.size
        result.add(Opcode3DS.CONSUME_TOP)
    }
}