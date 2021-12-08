package com.thane98.exalt.decompiler

import com.thane98.exalt.model.stmt.Stmt

class StatementAddressTracker {
    private val addresses = mutableMapOf<Stmt, Int>()

    fun register(stmt: Stmt, address: Int) {
        addresses[stmt] = address
    }

    fun lookup(stmt: Stmt): Int {
        return addresses[stmt]!!
    }

    fun contains(stmt: Stmt): Boolean {
        return stmt in addresses
    }
}