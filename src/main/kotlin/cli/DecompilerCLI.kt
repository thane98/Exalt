package cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import decompiler.DecompileError
import decompiler.decompile

class ExdArgs(parser: ArgParser) {
    val output by parser.storing(
        "-o", "--output",
        help = "output file symbol"
    ).default("a.exl")

    val source by parser.positional(
        "SOURCE",
        help = "source file symbol"
    )
}

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::ExdArgs).run {
        try {
            decompile(source, output)
        } catch (error: DecompileError) {
            println("Decompiling Failed!")
            if (error.address != -1)
                println("${error.message} at ${error.address}.")
            else
                println(error.message)
        }
    }
}