package lang.mango

import lang.mango.compiler.ByteEncoder
import lang.mango.compiler.MangoCompiler
import lang.mango.compiler.MangoLinker
import lang.mango.parser.MangoParser
import java.io.File

fun main(args: Array<String>) {
    require(args.size == 2) { "Usage: mangoc <source file> <output file>" }

    val sourceFile = File(args[0])

    val source = sourceFile.readText()

    val ast = MangoParser.parse(source)

    val chunks = MangoCompiler.compile(ast)

    val encoder = ByteEncoder

    val linked = MangoLinker(encoder).link(chunks)

    val bytes = encoder.encode(linked).array()

    // delete file if exists
    File(args[1]).delete()

    File(args[1]).writeBytes(bytes)
}