package com.thane98.exalt.model

data class VersionInfo(
    val textDataPointerAddress: Int,
    val functionTablePointerAddress: Int,
    val scriptTypeAddress: Int,
    val versionAddress: Int,
    val expectedVersion: Int,
    val textBeforeFunctions: Boolean,
    val padLastFunction: Boolean,
) {
    companion object {
        fun v1(): VersionInfo {
            return VersionInfo(
                0x24,
                0x28,
                0x20,
                0x18,
                0x20041125,
                textBeforeFunctions = true,
                padLastFunction = true
            )
        }

        fun v2(): VersionInfo {
            return VersionInfo(
                0x24,
                0x28,
                0x20,
                0x18,
                0x20061024,
                textBeforeFunctions = true,
                padLastFunction = true
            )
        }

        fun v3(): VersionInfo {
            return VersionInfo(
                0x20,
                0x1C,
                0x24,
                0x4,
                0x20110819,
                textBeforeFunctions = false,
                padLastFunction = false
            )
        }
    }
}