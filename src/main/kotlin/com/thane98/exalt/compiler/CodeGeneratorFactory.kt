package com.thane98.exalt.compiler

import com.thane98.exalt.interfaces.CodeGenerator
import com.thane98.exalt.interfaces.TextDataCreator
import com.thane98.exalt.model.Game
import com.thane98.exalt.model.Script
import com.thane98.exalt.model.VersionInfo

class CodeGeneratorFactory {
    companion object {
        fun buildGeneratorForScript(
            script: Script,
            textDataCreator: TextDataCreator = CachingTextDataCreator()
        ): CodeGenerator {
            return when (script.game) {
                Game.FE10 -> ConfigurableCodeGenerator(
                    VersionInfo.v2(),
                    V1HeaderSerializer(VersionInfo.v2()),
                    V1DeclToFunctionDataConverter(
                        textDataCreator,
                        V1ArgSerializer(),
                        V2CodeSerializer()
                    ),
                    V1FunctionSerializer(),
                    textDataCreator
                )
                Game.FE11, Game.FE12 -> ConfigurableCodeGenerator(
                    VersionInfo.v2(),
                    V1HeaderSerializer(VersionInfo.v2()),
                    V1DeclToFunctionDataConverter(
                        textDataCreator,
                        V1ArgSerializer(),
                        FE12CodeSerializer()
                    ),
                    V1FunctionSerializer(),
                    textDataCreator
                )
                Game.FE13, Game.FE14, Game.FE15 -> ConfigurableCodeGenerator(
                    VersionInfo.v3(),
                    V3HeaderSerializer(),
                    V3DeclToFunctionDataConverter(textDataCreator, V3ArgSerializer()),
                    V3FunctionSerializer(),
                    textDataCreator
                )
            }
        }
    }
}