package com.thane98.common

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class TranslationEngine {
    companion object {
        private val japaneseToEnglish = hashMapOf<String, String>()
        private val englishToJapanese = hashMapOf<String, String>()
        private var loaded = false

        private fun load() {
            val targetPath = Paths.get(System.getProperty("user.dir") + "/Translations.txt")
            if (Files.exists(targetPath)) {
                val translationLines = Files.readAllLines(targetPath, StandardCharsets.UTF_8)
                for (line in translationLines) {
                    val split = line.split(' ')
                    if (split.size >= 2) {
                        japaneseToEnglish[split[0]] = split[1]
                        englishToJapanese[split[1]] = split[0]
                    }
                }
            }
            loaded = true
        }

        fun toEnglish(input: String): String {
            if (!loaded)
                load()
            return if (japaneseToEnglish.containsKey(input))
                japaneseToEnglish[input]!!
            else
                input
        }

        fun toJapanese(input: String): String {
            if (!loaded)
                load()
            return if (englishToJapanese.containsKey(input))
                englishToJapanese[input]!!
            else
                input
        }
    }
}