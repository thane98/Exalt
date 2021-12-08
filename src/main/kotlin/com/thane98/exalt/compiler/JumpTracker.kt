package com.thane98.exalt.compiler

import com.thane98.exalt.model.JumpMarker

class JumpTracker {
    private val markers = mutableMapOf<String, MutableList<JumpMarker>>()
    private val labelAddresses = mutableMapOf<String, Int>()

    fun createMarker(label: String, jumpOffset: Int) {
        markers.getOrPut(label) { mutableListOf() }.add(JumpMarker(jumpOffset))
    }

    fun resolveLabel(label: String, offset: Int) {
        labelAddresses[label] = offset
    }

    fun resolveAllJumps(rawCode: MutableList<Byte>) {
        val unresolvedLabels = markers.keys - labelAddresses.keys
        if (unresolvedLabels.isNotEmpty()) {
            throw IllegalStateException("Cannot resolve jumps when there are unresolved labels: $unresolvedLabels")
        }
        for ((label, jumpMarkers) in markers) {
            val labelAddress = labelAddresses[label]!!
            for (jumpMarker in jumpMarkers) {
                val jumpAddress = jumpMarker.indexInOutput
                val jumpOffset = labelAddress - jumpAddress
                if (jumpOffset !in Short.MIN_VALUE..Short.MAX_VALUE) {
                    throw IllegalArgumentException("Jump exceeds CMVM limits, label=$label, markers=$jumpMarkers")
                }
                val rawJumpOffset = CodegenUtils.toBigEndianBytes(jumpOffset.toShort())
                rawCode[jumpAddress] = rawJumpOffset[0]
                rawCode[jumpAddress + 1] = rawJumpOffset[1]
            }
        }
    }

    fun reset() {
        markers.clear()
        labelAddresses.clear()
    }
}