package lang.mango.parser

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs

class MangoParserTests {

    private fun createBody(input: String): AST.Declaration.Function {
        val main = """
            fn main() {
                $input
            }
        """.trimIndent()
        val program = MangoParser.parse(main)
        assertEquals(1, program.functions.size)
        return program.functions[0]
    }

    @Test
    fun testEmpty () {
        val input = ""

        val result = Grammar.parseToEnd(input)

        assertIs<AST.Program>(result)
        assertEquals(0, result.functions.size)
    }

    @Test
    fun emptyMain() {
        val input = """
            fn main() {
            }
        """.trimIndent()

        val result = Grammar.parseToEnd(input) as AST.Program

        assertEquals(1, result.functions.size)
        val statement = result.functions[0]

        assertIs<AST.Declaration.Function>(statement)
        assertEquals("main", statement.identifier.name)
        assertEquals(0, statement.arguments.size)
        assertEquals(0, statement.body.size)
    }

    @Test
    fun assignment() {
        val function = createBody("""
            let a = 1
            let b = 2
        """.trimIndent())

        assertEquals(2, function.body.size)

        val a = function.body[0] as AST.Declaration.Variable
        assertEquals("a", a.identifier.name)
        assertIs<AST.Literal.Integer>(a.expression)
        assertEquals(1, a.expression.value)

        val b = function.body[1] as AST.Declaration.Variable
        assertEquals("b", b.identifier.name)
        assertIs<AST.Literal.Integer>(b.expression)
        assertEquals(2, b.expression.value)
    }

    @Test
    fun expressionAssignment() {
        val function = createBody("""
            let a = 1 + 2
        """.trimIndent())

        assertEquals(1, function.body.size)

        val a = function.body[0] as AST.Declaration.Variable
        assertEquals("a", a.identifier.name)
        assertIs<AST.BinaryOperation>(a.expression)
        val binary = a.expression
        assertIs<AST.Literal.Integer>(binary.left)
        assertEquals(1, binary.left.value)
        assertIs<AST.Literal.Integer>(binary.right)
        assertEquals(2, binary.right.value)
    }

    @Test
    fun functionCall() {
        val main = createBody("""
            let a = add(1, 2)
        """.trimIndent())

        assertEquals(1, main.body.size)

        val a = main.body[0] as AST.Declaration.Variable
        assertEquals("a", a.identifier.name)
        assertIs<AST.FunctionCall>(a.expression)

        val call = a.expression
        assertEquals("add", call.identifier.name)
        assertEquals(2, call.arguments.size)
        assertIs<AST.Literal.Integer>(call.arguments[0])
        assertEquals(1, (call.arguments[0] as AST.Literal.Integer).value)
        assertIs<AST.Literal.Integer>(call.arguments[1])
        assertEquals(2, (call.arguments[1] as AST.Literal.Integer).value)
    }

    @Test
    fun whenClause() {
        val main = createBody("""
            when (x > 1) foo()
            when (x < 1) { bar() }
            when (x == 1) {
                baz()
            }
        """.trimIndent())

        assertEquals(3, main.body.size)

        assertIs<AST.Control.When>(main.body[0])
        assertIs<AST.Control.When>(main.body[1])
        assertIs<AST.Control.When>(main.body[2])
    }

    @Test
    fun whileStatement() {
        val main = createBody("""
            while (x > 1) foo()
            while (x < 1) { bar() }
            while (x == 1) {
                baz()
            }
        """.trimIndent())

        assertEquals(3, main.body.size)

        assertIs<AST.Control.While>(main.body[0])
        assertIs<AST.Control.While>(main.body[1])
        assertIs<AST.Control.While>(main.body[2])
    }

    @Test
    fun returnStatement() {
        val main = createBody("""
            return 1
            
            when (x > 1) return foo()
            when (x < 1) { return bar() }
            when (x == 1) {
                return baz()
            }
            
            when (x > 1) return a
            when (x < 1) { return b }
            when (x == 1) {
                return c
            }
        """.trimIndent())

        assertEquals(7, main.body.size)
    }

    @Test
    fun fibonacci() {
        val input = """
            fn fib(n) {
                when (n == 0) return 0
                when (n == 1) return 1
                
                return fib(n - 1) + fib(n - 2)
            }
            
            fn main() {
                let n = input()
                return fib(n)
            }
        """.trimIndent()

        MangoParser.parse(input)
    }

    @Test
    fun nestedFunctions() {
        val input = """
            fn outer() {
                fn inner() {
                    return 1
                }
                
                return inner()
            }
        """.trimIndent()

        assertFails("Nested functions are not allowed") {
            MangoParser.parse(input)
        }
    }
}