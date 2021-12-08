package com.thane98.exalt.model

import com.thane98.exalt.model.decl.Annotation

data class CompilerFeatures(
    val useLongReturn: Boolean = false,
    val addFunctionNameToTextData: Boolean = false,
    val framePadding: Int = 0
) {
    companion object {
        fun fromAnnotations(annotations: List<Annotation>): CompilerFeatures {
            var useLongReturn = false
            var addFunctionNameToTextData = false
            var framePadding = 0
            for (annotation in annotations) {
                when (annotation.name) {
                    "LongReturn" -> { useLongReturn = true }
                    "ForceWriteName" -> { addFunctionNameToTextData = true }
                    "PadFrame" -> { framePadding = annotation.args.first().toInt() }
                }
            }
            return CompilerFeatures(
                useLongReturn,
                addFunctionNameToTextData,
                framePadding
            )
        }
    }
}