package com.thane98.exalt.ui.misc

import javafx.concurrent.Task
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder
import org.reactfx.Subscription
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors
import java.util.regex.Pattern

class AsyncSyntaxHighlighter(private val codeArea: CodeArea) {
    companion object {
        private val KEYWORDS = arrayOf(
            "event", "func", "const", "var",
            "let", "for", "while", "if",
            "else", "match", "return", "yield",
            "array", "goto", "label", "fix", "float"
        )

        private val KEYWORD_PATTERN = "\\b(" + KEYWORDS.joinToString("|") + ")\\b"
        private const val STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\""
        private const val COMMENT_PATTERN = "#[^\n]*"
        private const val FRAME_REF_PATTERN = "(\\$|\\$\\$)[0-9]+"
        private const val ANNOTATION_PATTERN = "@[a-zA-Z]+"

        private val PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<FRAMEREF>" + FRAME_REF_PATTERN + ")"
                    + "|(?<ANNOTATION>" + ANNOTATION_PATTERN + ")"
        )
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val subscription: Subscription

    init {
        subscription = codeArea.multiPlainChanges()
            .successionEnds(Duration.ofMillis(200))
            .supplyTask(this::computeHighlightingAsync)
            .awaitLatest(codeArea.multiPlainChanges())
            .filterMap { t ->
                if (t.isSuccess)
                    Optional.of(t.get())
                else
                    Optional.empty()
            }
            .subscribe(this::applyHighlighting)
    }

    fun terminate() {
        subscription.unsubscribe()
        executor.shutdownNow()
    }

    private fun computeHighlightingAsync(): Task<StyleSpans<Collection<String>>> {
        val text = codeArea.text
        val task = object : Task<StyleSpans<Collection<String>>>() {
            override fun call(): StyleSpans<Collection<String>> {
                return computeHighlighting(text)
            }
        }
        executor.execute(task)
        return task
    }

    private fun applyHighlighting(highlighting: StyleSpans<Collection<String>>) {
        codeArea.setStyleSpans(0, highlighting)
    }

    private fun computeHighlighting(text: String): StyleSpans<Collection<String>> {
        val matcher = PATTERN.matcher(text)
        var lastKwEnd = 0
        val spansBuilder = StyleSpansBuilder<Collection<String>>()
        while (matcher.find()) {
            val styleClass = (when {
                matcher.group("KEYWORD") != null -> "keyword"
                matcher.group("STRING") != null -> "string"
                matcher.group("COMMENT") != null -> "comment"
                matcher.group("FRAMEREF") != null -> "frame-ref"
                matcher.group("ANNOTATION") != null -> "annotation"
                else -> null
            })!!
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd)
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start())
            lastKwEnd = matcher.end()
        }
        spansBuilder.add(Collections.emptyList(), text.length - lastKwEnd)
        return spansBuilder.create()
    }
}