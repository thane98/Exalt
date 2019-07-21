package com.thane98.exalt.editor

import java.io.BufferedReader
import java.io.InputStreamReader

class CompletionManager {
    companion object {
        val fatesFunctions: HashSet<String> by lazy {
            val path = this::class.java.getResourceAsStream("FatesFunctions.txt")
            val reader = BufferedReader(InputStreamReader(path))
            val lines = hashSetOf<String>()
            reader.forEachLine { line ->
                if (line.isNotEmpty())
                    lines.add(line)
            }
            reader.close()
            lines
        }
    }
}