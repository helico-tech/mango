package lang.mango.vm

import lang.mango.compiler.ByteEncoder
import lang.mango.compiler.MangoCompiler
import lang.mango.compiler.MangoLinker
import lang.mango.parser.MangoParser
import org.junit.Test
import kotlin.test.assertEquals

class ByteCodeVirtualMachineTests {

    @Test
    fun fibonacci() {
        val code = """
            fn fib(n) {
                when (n == 0) return 0
                when (n == 1) return 1

                return fib(n - 1) + fib(n - 2)
            }

            fn main() {
                return fib(30)
            }
        """.trimIndent()

        val ast = MangoParser.parse(code)

        val asm = MangoCompiler.compile(ast)

        val encoder = ByteEncoder

        val linked = MangoLinker(encoder).link(asm)

        val byteCode = encoder.encode(linked).array()

        val vm = ByteCodeVirtualMachine(byteCode)

        vm.run()

        assertEquals(832040, vm.stack.first())
    }
}