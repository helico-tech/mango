package lang.mango.compiler

import lang.mango.parser.MangoParser
import kotlin.test.*

class VirtualMachineTests {

    private fun create(input: String): VirtualMachine {
        val main = """
            fn main() {
                $input
            }
        """.trimIndent()
        val program = MangoParser.parse(main)
        assertEquals(1, program.functions.size)
        val chunk = MangoCompiler.compile(program)[0]
        return VirtualMachine(chunk.instructions)
    }

    @Test
    fun empty() {
        val vm = create("")
        vm.run()
        assertEquals(0, vm.stack.size)
    }

    @Test
    fun assignment() {
        val vm = create("""
            let a = 1
            let b = a + 2
        """.trimIndent())

        vm.run()
    }
}