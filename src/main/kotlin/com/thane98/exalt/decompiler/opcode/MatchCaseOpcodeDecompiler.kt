package com.thane98.exalt.decompiler.opcode

import com.thane98.exalt.error.NegativeBranchException
import com.thane98.exalt.interfaces.OpcodeDecompiler
import com.thane98.exalt.model.CodeDecompilerState
import com.thane98.exalt.model.DecompileStepResult
import com.thane98.exalt.model.Operator
import com.thane98.exalt.model.expr.Binary
import com.thane98.exalt.model.stmt.Case
import com.thane98.exalt.model.stmt.Goto

class MatchCaseOpcodeDecompiler(
    private val dupOpcode: Int,
    private val consumeOpcode: Int,
    private val gotoOpcode: Int,
) : OpcodeDecompiler {
    override fun decompile(state: CodeDecompilerState): DecompileStepResult {
        // Get the condition for this case.
        val rawCondition = state.expressions.pop() as Binary
        if (rawCondition.op != Operator.EQUAL) {
            throw IllegalStateException("Invalid match case sequence - op only supports equals.")
        }
        val condition = rawCondition.right

        // Figure out where the body starts.
        // Expected sequence is:
        // JumpNotZero {offset of body, always after read + 3} (what this compiler acts on)
        // Goto {offset of next case}
        // body
        val bodyOffset = state.reader.readBigEndianShort() - 2
        if (bodyOffset != 3) {
            throw IllegalStateException("Invalid match case sequence - bad body offset.")
        }

        // Get the offset of the next case so, we know how far to read.
        val potentialGotoOpcode = state.reader.readByte().toInt()
        if (potentialGotoOpcode != gotoOpcode) {
            throw IllegalStateException("Invalid match case sequence - no goto next case op.")
        }
        val offsetOfNextCase = state.reader.readBigEndianShort() - 2
        if (offsetOfNextCase < 0) {
            throw NegativeBranchException()
        }
        val addressOfNextCase = state.reader.position + offsetOfNextCase

        // Read the case body.
        state.blocks.push()
        state.codeDecompiler.decompileBlock(state, offsetOfNextCase)
        val body = state.blocks.pop()

        // Body *should* end with goto {end of match}
        // Get rid of that since it shouldn't be included in the AST.
        var isEndOfMatch: Boolean
        val endAddress: Int
        if (body.contents.isNotEmpty()) {
            val last = body.contents.last()
            if (last !is Goto || last.symbol!!.address!! < 0) {
                throw IllegalStateException("Case body did not end with a positive goto.")
            }
            body.contents.removeLast()

            endAddress = last.symbol!!.address!!
            isEndOfMatch = endAddress == addressOfNextCase
        } else {
            throw IllegalStateException("Empty case body.")
        }

        // End of this case, but we might have other stuff to do here.
        // If next is NOT a dup opcode, and we aren't at the end of the match yet,
        // then this must be a default case.
        val nextOpcode = state.reader.readByte().toInt()
        if (!isEndOfMatch && nextOpcode != dupOpcode) {
            assert(isEndOfMatch)
            state.reader.position -= 1
            state.blocks.push()
            state.codeDecompiler.decompileBlock(state, endAddress - state.reader.position)

            val block = state.blocks.pop()
            val last = block.contents.last()
            if (last !is Goto || last.symbol!!.address!! != state.reader.position) {
                throw IllegalStateException("Default case did not end with goto -> {end of match}")
            }
            block.contents.removeLast()
            state.matches.addDefault(block)
            isEndOfMatch = true
        } else if (isEndOfMatch) {
            state.reader.position -= 1
        } else {
            state.expressions.push(state.matches.currentSwitch())
        }


        state.matches.addCase(Case(condition, body))
        return if (isEndOfMatch) {
            state.blocks.line(state.matches.complete())
            state.labelVendor.purge(state.reader.position)
            val next = state.reader.readByte().toInt()
            assert(next == consumeOpcode)
            DecompileStepResult.STMT
        } else {
            DecompileStepResult.OTHER
        }
    }
}