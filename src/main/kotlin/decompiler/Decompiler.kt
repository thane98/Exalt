package decompiler

import ast.PrettyPrinter
import java.nio.file.Files
import java.nio.file.Paths

fun decompile(source: String, output: String) {
    val file = Paths.get(source)
    if (!Files.exists(file) || Files.isDirectory(file))
        throw DecompileError("Unable to open source file.", -1)
    val script = Decompiler3DS.decompile(Files.readAllBytes(file))
    Files.writeString(Paths.get(output), PrettyPrinter.print(script))
}