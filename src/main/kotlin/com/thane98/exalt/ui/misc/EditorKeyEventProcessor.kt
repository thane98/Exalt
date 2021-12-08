package com.thane98.exalt.ui.misc

import javafx.application.Platform
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import org.fxmisc.richtext.CodeArea
import java.util.regex.Pattern

class EditorKeyEventProcessor {
    companion object {
        private val WHITESPACE = Pattern.compile("^\\s+")

        fun setup(codeArea: CodeArea) {
            val processor = EditorKeyEventProcessor()
            processor.installHandlers(codeArea)
        }
    }

    fun installHandlers(codeArea: CodeArea) {
        codeArea.addEventFilter(KeyEvent.KEY_PRESSED) {
            when(it.code) {
                KeyCode.QUOTE -> {
                    if (it.isShiftDown)
                        processOpen(it, codeArea, "\"")
                    else
                        processOpen(it, codeArea, "'")
                }
                KeyCode.DIGIT9 -> {
                    if (it.isShiftDown)
                        processOpen(it, codeArea, ")")
                }
                KeyCode.OPEN_BRACKET -> {
                    if (it.isShiftDown)
                        processOpen(it, codeArea, "}")
                    else
                        processOpen(it, codeArea, "]")
                }
                KeyCode.TAB -> processTab(it, codeArea)
                KeyCode.ENTER -> processEnter(codeArea)
                KeyCode.BACK_SPACE -> processBackspace(it, codeArea)
                else -> {}
            }
        }
    }

    private fun processOpen(keyEvent: KeyEvent, codeArea: CodeArea, closing: String) {
        Platform.runLater {
            val position = codeArea.caretPosition
            codeArea.insertText(position, closing)
            codeArea.moveTo(position)
        }
        keyEvent.consume()
    }

    private fun processTab(keyEvent: KeyEvent, codeArea: CodeArea) {
        Platform.runLater { codeArea.insertText(codeArea.caretPosition, "    ") }
        keyEvent.consume()
    }

    private fun processEnter(codeArea: CodeArea) {
        val targetParagraph = codeArea.getParagraph(codeArea.currentParagraph)
        val matcher = WHITESPACE.matcher(targetParagraph.segments[0])
        if (matcher.find()) {
            val indent = if (targetParagraph.segments[0].endsWith("{")) {
                matcher.group() + "    "
            } else {
                matcher.group()
            }
            Platform.runLater { codeArea.insertText(codeArea.caretPosition, indent) }
        }
    }

    private fun processBackspace(keyEvent: KeyEvent, codeArea: CodeArea) {
        val targetParagraph = codeArea.getParagraph(codeArea.currentParagraph)
        if (targetParagraph.segments.size == 1
            && targetParagraph.segments[0].isNotEmpty()
            && targetParagraph.segments[0].isBlank()) {
            val segment = targetParagraph.segments[0]
            val startWithNewLine = codeArea.caretPosition - segment.length - 1
            val start = if (startWithNewLine < 0) 0 else startWithNewLine
            codeArea.deleteText(
                start,
                codeArea.caretPosition
            )
            keyEvent.consume()
        } else if (atEndOfIndent(codeArea)) {
            codeArea.deleteText(codeArea.caretPosition - 4, codeArea.caretPosition)
            keyEvent.consume()
        }
    }

    private fun atEndOfIndent(codeArea: CodeArea): Boolean {
        val pos = codeArea.caretPosition
        if (pos - 4 < 0)
            return false
        return codeArea.text.slice(pos - 4 until pos) == "    "
    }
}