package com.thane98.exalt.decompiler

import com.thane98.exalt.model.InProgressMatch
import com.thane98.exalt.model.expr.Expr
import com.thane98.exalt.model.stmt.Case
import com.thane98.exalt.model.stmt.Match
import com.thane98.exalt.model.stmt.Stmt

class MatchStack {
    private val matches = ArrayDeque<Match>()

    fun start(switch: Expr) {
        matches.addLast(Match(switch, mutableListOf(), null))
    }

    fun complete(): Match {
        return matches.last()
    }

    fun addCase(case: Case) {
        matches.last().cases.add(case)
    }

    fun addDefault(stmt: Stmt) {
        matches.last().default = stmt
    }

    fun currentSwitch(): Expr {
        return matches.last().switch
    }
}