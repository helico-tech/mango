package lang.mango.compiler

import lang.mango.parser.AST
import lang.mango.parser.MangoParser
import lang.mango.parser.Tokens
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
        val compiler = MangoCompiler()
        val function = AST.Declaration.Function(AST.Identifier("main"), emptyList(), AST.Block(emptyList()))
        val chunk = compiler.function(function)

        assertEquals("main", chunk.name)
        assertEquals(0, chunk.instructions.size)
    }

    @Test()
    fun saveLocal() {
        val compiler = MangoCompiler()
        val function = create("let a = b + 1")
        val chunk = compiler.function(function)

        assertEquals(4, chunk.instructions.size)
        assertEquals(Load.Constant(1), chunk.instructions[0])
        assertEquals(Load.Local("b"), chunk.instructions[1])
        assertEquals(Arithmetic("plus"), chunk.instructions[2])
        assertEquals(Store("a"), chunk.instructions[3])
    }
}