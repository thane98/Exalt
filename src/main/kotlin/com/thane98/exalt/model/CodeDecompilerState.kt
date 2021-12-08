package com.thane98.exalt.model

import com.thane98.exalt.common.SymbolTable
import com.thane98.exalt.decompiler.*
import com.thane98.exalt.interfaces.BinaryReader
import com.thane98.exalt.interfaces.CodeDecompiler
import com.thane98.exalt.interfaces.TextDataVendor
import com.thane98.exalt.model.decl.Annotation
import com.thane98.exalt.model.symbol.FunctionSymbol

data class CodeDecompilerState(
    val reader: BinaryReader,
    val textDataVendor: TextDataVendor,
    val functionIdToSymbol: Map<Int, FunctionSymbol>,
    val symbolTable: SymbolTable,
    val codeDecompiler: CodeDecompiler,
    val requestedAnnotations: MutableSet<Annotation> = mutableSetOf(),
    val statementAddressTracker: StatementAddressTracker = StatementAddressTracker(),
    val labelVendor: LabelVendor = LabelVendor(),
    val expressions: ExprStack = ExprStack(),
    val blocks: BlockStack = BlockStack(),
    val matches: MatchStack = MatchStack(),
    var assignmentState: AssignmentState = AssignmentState.NORMAL,
)
