package com.thane98.exalt.model

import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.stmt.Case
import com.thane98.exalt.model.stmt.Stmt

data class InProgressMatch(
    val switch: Expr,
    val cases: MutableList<Case>,
    var default: Stmt?,
) {
    companion object {
        fun start(switch: Expr): InProgressMatch {
            return InProgressMatch(switch, mutableListOf(), null)
        }
    }
}