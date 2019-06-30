package decompiler

import ast.PrettyPrinter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

fun decompile(source: String, enableExperimental: Boolean): String {
    val file = Paths.get(source)
    if (!Files.exists(file) || Files.isDirectory(file))
        throw DecompileError("Unable to open source file.", -1)
    val script = Decompiler3DS.decompile(Files.readAllBytes(file), enableExperimental)
    return PrettyPrinter.print(script)
}

fun decompile(source: String, output: String, enableExperimental: Boolean) {
    Files.write(Paths.get(output), decompile(source, enableExperimental).toByteArray(StandardCharsets.UTF_8))
}