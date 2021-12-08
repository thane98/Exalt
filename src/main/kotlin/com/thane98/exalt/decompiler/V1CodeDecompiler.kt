package com.thane98.exalt.decompiler

import com.thane98.exalt.common.SymbolTable
import com.thane98.exalt.decompiler.opcode.*
import com.thane98.exalt.interfaces.BinaryReader
import com.thane98.exalt.interfaces.TextDataVendor
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.IntType
import com.thane98.exalt.model.Operator
import com.thane98.exalt.model.symbol.FunctionSymbol

class V1CodeDecompiler(
    reader: BinaryReader,
    textDataVendor: TextDataVendor,
    functionIdToSymbol: Map<Int, FunctionSymbol>,
    symbolTable: SymbolTable,
) : AbstractCodeDecompiler(reader, textDataVendor, functionIdToSymbol, symbolTable) {
    companion object {
        private val decompilers = listOf(
            DoneOpcodeDecompiler(),
            VarRefOpcodeDecompiler(IntType.BYTE),
            VarRefOpcodeDecompiler(IntType.SHORT),
            ArrayRefOpcodeDecompiler(IntType.BYTE),
            ArrayRefOpcodeDecompiler(IntType.SHORT),
            PointerRefOpcodeDecompiler(IntType.BYTE),
            PointerRefOpcodeDecompiler(IntType.SHORT),
            VarRefOpcodeDecompiler(IntType.BYTE, true),
            VarRefOpcodeDecompiler(IntType.SHORT, true),
            ArrayRefOpcodeDecompiler(IntType.BYTE, true),
            ArrayRefOpcodeDecompiler(IntType.SHORT, true),
            PointerRefOpcodeDecompiler(IntType.BYTE, true),
            PointerRefOpcodeDecompiler(IntType.SHORT, true),
            VarRefOpcodeDecompiler(IntType.BYTE, global = true),
            VarRefOpcodeDecompiler(IntType.SHORT, global = true),
            ArrayRefOpcodeDecompiler(IntType.BYTE, global = true),
            ArrayRefOpcodeDecompiler(IntType.SHORT, global = true),
            PointerRefOpcodeDecompiler(IntType.BYTE, global = true),
            PointerRefOpcodeDecompiler(IntType.SHORT, global = true),
            VarRefOpcodeDecompiler(IntType.BYTE, addr = true, global = true),
            VarRefOpcodeDecompiler(IntType.SHORT, addr = true, global = true),
            ArrayRefOpcodeDecompiler(IntType.BYTE, addr = true, global = true),
            ArrayRefOpcodeDecompiler(IntType.SHORT, addr = true, global = true),
            PointerRefOpcodeDecompiler(IntType.BYTE, addr = true, global = true),
            PointerRefOpcodeDecompiler(IntType.SHORT, addr = true, global = true),
            LoadIntOpcodeDecompiler(IntType.BYTE),
            LoadIntOpcodeDecompiler(IntType.SHORT),
            LoadIntOpcodeDecompiler(IntType.INT),
            LoadStringOpcodeDecompiler(IntType.BYTE),
            LoadStringOpcodeDecompiler(IntType.SHORT),
            LoadStringOpcodeDecompiler(IntType.INT),
            ShorthandAssignmentSignalOpcodeDecompiler(),
            ConsumeOpcodeDecompiler(),
            AssignmentOpcodeDecompiler(),
            BinaryOpcodeDecompiler(Operator.ADD),
            BinaryOpcodeDecompiler(Operator.SUBTRACT),
            BinaryOpcodeDecompiler(Operator.MULTIPLY),
            BinaryOpcodeDecompiler(Operator.DIVIDE),
            BinaryOpcodeDecompiler(Operator.MODULO),
            UnaryOpcodeDecompiler(Operator.NEGATE),
            UnaryOpcodeDecompiler(Operator.BINARY_NOT),
            UnaryOpcodeDecompiler(Operator.LOGICAL_NOT),
            BinaryOpcodeDecompiler(Operator.BINARY_OR),
            BinaryOpcodeDecompiler(Operator.BINARY_AND),
            BinaryOpcodeDecompiler(Operator.XOR),
            BinaryOpcodeDecompiler(Operator.LEFT_SHIFT),
            BinaryOpcodeDecompiler(Operator.RIGHT_SHIFT),
            BinaryOpcodeDecompiler(Operator.EQUAL),
            BinaryOpcodeDecompiler(Operator.NOT_EQUAL),
            BinaryOpcodeDecompiler(Operator.LESS_THAN),
            BinaryOpcodeDecompiler(Operator.LESS_THAN_OR_EQUAL_TO),
            BinaryOpcodeDecompiler(Operator.GREATER_THAN),
            BinaryOpcodeDecompiler(Operator.GREATER_THAN_OR_EQUAL_TO),
            BuiltInFunctionDecompiler(FunctionSymbol("streq", 2)),
            BuiltInFunctionDecompiler(FunctionSymbol("strne", 2)),
            V1CallByIdOpcodeDecompiler(),
            CallByNameOpcodeDecompiler(),
            ReturnDecompiler(),
            GotoOpcodeDecompiler(),
            null, // TODO: Investigate how V1 does match/case
            ShortCircuitedBinaryOpcodeDecompiler(Operator.LOGICAL_OR),
            IfOpcodeDecompiler(),
            ShortCircuitedBinaryOpcodeDecompiler(Operator.LOGICAL_AND),
            YieldOpcodeDecompiler(),
            null,
            PrintfOpcodeDecompiler(),
        )
    }

    override fun decompileOpcode(state: CodeDecompilerState, opcode: Int): DecompileStepResult {
        val decompiler = if (opcode < 0 || opcode >= decompilers.size) {
            null
        } else {
            decompilers[opcode]
        }
        if (decompiler == null) {
            throw IllegalArgumentException("Bad / unrecognized opcode '0x${opcode.toString(16)}'")
        }
        return decompiler.decompile(state)
    }
}