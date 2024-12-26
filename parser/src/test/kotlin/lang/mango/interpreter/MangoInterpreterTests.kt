package lang.mango.interpreter

import lang.mango.parser.MangoParser
import kotlin.test.Test
import kotlin.test.assertEquals

class MangoInterpreterTests {

    @Test
    fun emptyProgram() {
        val interpreter = MangoInterpreter()
        val program = MangoParser.parse("""
            fn main() {}
        """.trimIndent())

        val result = interpreter.interpret(program)
        assertEquals(0, result)
    }

    @Test
    fun assignment() {
        val interpreter = MangoInterpreter()
        val program = MangoParser.parse("""
            fn main() {
                let a = 1
                let b = 2
                return a + b
            }
        """.trimIndent())

        val result = interpreter.interpret(program)
        assertEquals(3, result)
    }

    @Test
    fun fibonacci() {
        val interpreter = MangoInterpreter()
        val program = MangoParser.parse("""
            fn fib(n) {
                when (n == 0) return 0
                when (n == 1) return 1
                
                return fib(n - 1) + fib(n - 2)
            }
            
            fn main() {
                return fib(20)
            }
        """.trimIndent())

        val result = interpreter.interpret(program)
        assertEquals(6765, result)
    }
}