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

    val experimental by parser.flagging(
        "-e", "--experimental",
        help = "enable experimental code transformations"
    )

    val source by parser.positional(
        "SOURCE",
        help = "source file symbol"
    )
}

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::ExdArgs).run {
        try {
            decompile(source, output, experimental)
        } catch (error: DecompileError) {
            println("Decompiling Failed!")
            if (error.address != -1)
                println("${error.message} at ${error.address}.")
            else
                println(error.message)
        }
    }
}