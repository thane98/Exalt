package com.thane98.exalt.model.stmt

import com.thane98.exalt.model.expr.Expr

data class Case(val condition: Expr, val body: Stmt)
