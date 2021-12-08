package com.thane98.exalt.compiler

import com.thane98.exalt.model.Script

class PostParseProcessor {
    companion object {
        fun process(script: Script) {
            CallIdCalculator.calculate(script)
            FrameDataCalculator.calculate(script)
        }
    }
}