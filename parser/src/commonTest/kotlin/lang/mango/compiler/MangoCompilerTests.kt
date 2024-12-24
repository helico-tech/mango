package lang.mango.compiler

import lang.mango.parser.AST
import lang.mango.parser.MangoParser
import kotlin.test.*

class MangoCompilerTests {

    private fun create(input: String): AST.Declaration.Function {
        val main = """
            fn main() {
                $input
            }
        """.trimIndent()
        val program = MangoParser.parse(main)
        assertEquals(1, program.functions.size)
        return program.functions[0]
    }

    @Test()
    fun emptyFunction() {
        val function = AST.Declaration.Function(AST.Identifier("main"), emptyList(), AST.Block(emptyList()))
        val chunk = FunctionCompiler(function).compile()

        assertEquals("main", chunk.name)
        assertEquals(0, chunk.instructions.size)
    }

    @Test()
    fun saveLocal() {
        val function = create("""
            let a = 1
            let b = a + 2
        """.trimIndent())
        val chunk = FunctionCompiler(function).compile()

        assertEquals(6, chunk.instructions.size)

        assertEquals(Push(1), chunk.instructions[0])
        assertEquals(Store("a", 1), chunk.instructions[1])
        assertEquals(Push(2), chunk.instructions[2])
        assertEquals(Load("a", 1), chunk.instructions[3])
        assertEquals(Arithmetic("plus"), chunk.instructions[4])
        assertEquals(Store("b", 0), chunk.instructions[5])
    }

    @Test
    fun basicMain() {
        val input = """
            fn main() {
                let a = 1
                let b = 2
                let c = a + b
                
                return c
            }
        """.trimIndent()

        val chunks = MangoCompiler.compile(MangoParser.parse(input))

        assertEquals(1, chunks.size)

        val chunk = chunks[0]
        assertEquals("main", chunk.name)
        assertEquals(12, chunk.instructions.size)
    }
}