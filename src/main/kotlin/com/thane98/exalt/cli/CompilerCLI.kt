package com.thane98.exalt.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import com.thane98.exalt.compiler.compile

class ExcArgs(parser: ArgParser) {
    val output by parser.storing(
        "-o", "--output",
        help = "output file symbol"
    ).default("a.cmb")

    val source by parser.positional(
        "SOURCE",
        help = "source file symbol"
    )
}

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::ExcArgs).run {
        val runResult = compile(source, output)
        if (!runResult.successful()) {
            print(runResult.log.dump())
            System.exit(1)
        }
    }
}