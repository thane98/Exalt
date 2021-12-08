package com.thane98.exalt.compiler

import com.thane98.exalt.model.CodegenState
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CodegenUtils {
    companion object {
        fun toLittleEndianBytes(value: Short): ByteArray {
            return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array()
        }

        fun toLittleEndianBytes(value: Int): ByteArray {
            return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
        }

        fun toLittleEndianBytes(value: Float): ByteArray {
            return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array()
        }

        fun toBigEndianBytes(value: Int): ByteArray {
            return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(value).array()
        }

        fun toBigEndianBytes(value: Short): ByteArray {
            return ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort(value).array()
        }

        fun generateVariableLength(state: CodegenState, operand: Int, byteOp: Byte, shortOp: Byte) {
            if (operand < Byte.MAX_VALUE && operand > Byte.MIN_VALUE) {
                state.output.add(byteOp)
                state.output.add(operand.toByte())
            } else if (operand < Short.MAX_VALUE && operand > Short.MIN_VALUE) {
                state.output.add(shortOp)
                for (b in toBigEndianBytes(operand.toShort())) {
                    state.output.add(b)
                }
            } else {
                throw IllegalArgumentException("Operand $operand is too large to use for code generation.")
            }
        }

        fun generateVariableLength(
            state: CodegenState,
            operand: Int,
            byteOp: Byte,
            shortOp: Byte,
            intOp: Byte,
        ) {
            if (operand <= Byte.MAX_VALUE && operand >= Byte.MIN_VALUE) {
                state.output.add(byteOp)
                state.output.add(operand.toByte())
            } else if (operand <= Short.MAX_VALUE && operand >= Short.MIN_VALUE) {
                state.output.add(shortOp)
                for (b in toBigEndianBytes(operand.toShort())) {
                    state.output.add(b)
                }
            } else {
                state.output.add(intOp)
                for (b in toBigEndianBytes(operand)) {
                    state.output.add(b)
                }
            }
        }
    }
}