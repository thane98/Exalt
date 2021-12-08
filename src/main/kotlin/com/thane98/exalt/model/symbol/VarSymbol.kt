package com.thane98.exalt.model.symbol

data class VarSymbol(override val name: String, val global: Boolean, var frameId: Int? = null) : Symbol {
    companion object {
        private const val ANONYMOUS_VAR_PREFIX = "__anon_v__"

        fun anonymousName(frameId: Int): String {
            return "$ANONYMOUS_VAR_PREFIX$frameId"
        }

        fun parameterName(frameId: Int): String {
            return "v$frameId"
        }
    }

    override fun toString(): String {
        return if (name.startsWith(ANONYMOUS_VAR_PREFIX) && frameId != null) {
            val prefix = if (global) "$$" else "$"
            "$prefix$frameId"
        } else {
            name
        }
    }
}