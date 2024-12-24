package lang.mango.compiler

import lang.mango.parser.AST
import lang.mango.parser.MangoParser
import kotlin.test.Test
import kotlin.test.assertEquals

class MangoCompilerTests {

    private fun create(source: String, bootstrap: Boolean = true): Pair<List<ASM.Chunk>, List<ASM>> {
        val ast = MangoParser.parse(source)
        val chunks = MangoCompiler.compile(ast, bootstrap = bootstrap)
        val linked = MangoCompiler.link(chunks)
        return chunks to linked
    }

    @Test
    fun empty() {
        val result = MangoCompiler.compile(AST.Program(emptyList()), bootstrap = false)
        assertEquals(0, result.size)
    }

    @Test
    fun emptyMainWithoutBootstrap() {
        val ast = MangoParser.parse("""
            fn main() {}
        """.trimIndent())

        val result = MangoCompiler.compile(ast, bootstrap = false)

        val chunk = result[0] as ASM.Chunk.Function
        assertEquals("main", chunk.name)
        assertEquals(1, chunk.instructions.size)
        assertEquals(ASM.Jump, chunk.instructions[0].instruction)
    }

    @Test
    fun emptyMainWithBootstrap() {
        val (result, linked) = create("""
            fn main() {}
        """.trimIndent())

        assertEquals(2, result.size)

        val pretty = result.toPrettyString()

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