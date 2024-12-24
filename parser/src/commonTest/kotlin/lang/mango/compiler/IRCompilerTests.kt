package lang.mango.compiler

import lang.mango.parser.AST
import lang.mango.parser.MangoParser
import kotlin.test.Test
import kotlin.test.assertEquals

class IRCompilerTests {

    private fun create(source: String): AST.Program {
        return MangoParser.parse(source)
    }

    @Test
    fun empty() {
        val result = IRCompiler.compile(AST.Program(emptyList()), bootstrap = false)
        assertEquals(0, result.size)
    }

    @Test
    fun emptyMainWithoutBootstrap() {
        val ast = create("""
            fn main() {}
        """.trimIndent())

        val result = IRCompiler.compile(ast, bootstrap = false)
        assertEquals(1, result.size)

        val chunk = result[0] as IR.Chunk.Function
        assertEquals("main", chunk.name)
        assertEquals(0, chunk.instructions.size)
    }

    @Test
    fun emptyMainWithBootstrap() {
        val ast = create("""
            fn main() {}
        """.trimIndent())

        val result = IRCompiler.compile(ast, bootstrap = true)
        assertEquals(2, result.size)

        val pretty = result.toPrettyString()
        val linked = IRCompiler.link(result)

        assertEquals(6, linked.size)
        assertEquals(IR.Load.Constant(0), linked[0])
        assertEquals(IR.Load.Constant(4), linked[1])
        assertEquals(IR.Load.Constant(5), linked[2])
        assertEquals(IR.Jump, linked[3])
        assertEquals(IR.Exit, linked[4])
        assertEquals(IR.Jump, linked[5])
    }
}