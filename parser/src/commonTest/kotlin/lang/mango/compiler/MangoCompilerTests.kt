package lang.mango.compiler

import lang.mango.parser.AST
import lang.mango.parser.MangoParser
import kotlin.test.Test
import kotlin.test.assertEquals

class MangoCompilerTests {

    private fun create(source: String): AST.Program {
        return MangoParser.parse(source)
    }

    @Test
    fun empty() {
        val result = MangoCompiler.compile(AST.Program(emptyList()), bootstrap = false)
        assertEquals(0, result.size)
    }

    @Test
    fun emptyMainWithoutBootstrap() {
        val ast = create("""
            fn main() {}
        """.trimIndent())

        val result = MangoCompiler.compile(ast, bootstrap = false)
        assertEquals(1, result.size)

        val chunk = result[0] as ASM.Chunk.Function
        assertEquals("main", chunk.name)
        assertEquals(1, chunk.instructions.size)
        assertEquals(ASM.Jump, chunk.instructions[0].instruction)
    }

    @Test
    fun emptyMainWithBootstrap() {
        val ast = create("""
            fn main() {}
        """.trimIndent())

        val result = MangoCompiler.compile(ast, bootstrap = true)
        assertEquals(2, result.size)

        val pretty = result.toPrettyString()
        val linked = MangoCompiler.link(result)

        assertEquals(6, linked.size)
        assertEquals(ASM.Load.Constant(0), linked[0])
        assertEquals(ASM.Load.Constant(4), linked[1])
        assertEquals(ASM.Load.Constant(5), linked[2])
        assertEquals(ASM.Jump, linked[3])
        assertEquals(ASM.Exit, linked[4])
        assertEquals(ASM.Jump, linked[5])

        val vm = VirtualMachine(linked)
        vm.run()
        assertEquals(6, vm.ip)
        assertEquals(0, vm.stack.first())
    }
}