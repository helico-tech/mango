package lang.mango

import lang.mango.compiler.MangoCompiler
import lang.mango.compiler.MangoLinker
import lang.mango.compiler.NullEncoder
import lang.mango.interpreter.MangoInterpreter
import lang.mango.parser.MangoParser
import lang.mango.vm.ASMVirtualMachine
import lang.mango.vm.ByteCodeVirtualMachine
import java.io.File

fun main(args: Array<String>) {
    require(args.size == 1) { "Usage: mango <compiled file or interpreted file>" }

    val sourceFile = File(args[0])

    if (sourceFile.extension == "mc") {
        val source = sourceFile.readBytes()

        val vm = ByteCodeVirtualMachine(source)

        vm.run()

        println(vm.stack.first())

        return
    } else {
        val source = sourceFile.readText()

        val interpreter = MangoInterpreter()

        val ast = MangoParser.parse(source)

        val result = interpreter.interpret(ast)

        println(result)
    }
}