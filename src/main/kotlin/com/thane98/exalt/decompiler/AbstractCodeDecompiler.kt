package com.thane98.exalt.decompiler

import com.thane98.exalt.common.SymbolTable
import com.thane98.exalt.interfaces.BinaryReader
import com.thane98.exalt.interfaces.CodeDecompiler
import com.thane98.exalt.interfaces.TextDataVendor
import com.thane98.exalt.model.CodeDecompileResult
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.expr.Literal
import com.thane98.exalt.model.stmt.Block
import com.thane98.exalt.model.stmt.Return
import com.thane98.exalt.model.stmt.Stmt
import com.thane98.exalt.model.symbol.FunctionSymbol

abstract class AbstractCodeDecompiler(
    private val reader: BinaryReader,
    private val textDataVendor: TextDataVendor,
    private val functionIdToSymbol: Map<Int, FunctionSymbol>,
    private val symbolTable: SymbolTable,
) : CodeDecompiler {
    override fun decompile(address: Int): CodeDecompileResult {
        val state = CodeDecompilerState(
            reader,
            textDataVendor,
            functionIdToSymbol,
            symbolTable,
            this,
        )
        reader.position = address

        var stmt: Stmt?
        do {
            stmt = decompileNextStatement(state)
        } while (stmt != null)

        val block = state.blocks.pop()
        LabelResolver.resolve(block, state.labelVendor.allLabels(), state.statementAddressTracker)
        WhileSequenceFolder.fold(block)
        LabelPruner.prune(block)
        if (block.contents.size > 0) {
            val last = block.contents.last()
            if (last is Return && last.value == Literal.ofInt(0)) {
                block.contents.removeLast()
            }
        }
        return CodeDecompileResult(block, state.requestedAnnotations)
    }

    override fun decompileUntil(state: CodeDecompilerState, offset: Int) {
        val stop = state.reader.position + offset
        while (state.reader.position != stop) {
            decompileNext(state)
        }
    }

    override fun decompileBlock(state: CodeDecompilerState, offset: Int) {
        val stop = state.reader.position + offset
        while (state.reader.position != stop) {
            decompileNextStatement(state)
        }
    }

    override fun decompileNextStatement(state: CodeDecompilerState): Stmt? {
        var isFirstIteration = true // TODO: Gack
        val nextStatementAddress = state.reader.position
        var decompileStepResult: DecompileStepResult
        do {
            decompileStepResult = decompileNext(state)
            if (isFirstIteration && decompileStepResult == DecompileStepResult.DONE) {
                return null
            }
            isFirstIteration = false
        } while (decompileStepResult != DecompileStepResult.STMT)
        state.statementAddressTracker.register(state.blocks.peekLine(), nextStatementAddress)
        return state.blocks.peekLine()
    }

    override fun decompileNext(state: CodeDecompilerState): DecompileStepResult {
        return decompileOpcode(state, state.reader.readByte().toInt())
    }

    protected abstract fun decompileOpcode(state: CodeDecompilerState, opcode: Int): DecompileStepResult
}