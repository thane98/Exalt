package com.thane98.exalt.model

data class HardCodedTextData(
    val rawTextData: ByteArray,
    val offsetMappings: Map<String, Int>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HardCodedTextData

        if (!rawTextData.contentEquals(other.rawTextData)) return false
        if (offsetMappings != other.offsetMappings) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rawTextData.contentHashCode()
        result = 31 * result + offsetMappings.hashCode()
        return result
    }
}
