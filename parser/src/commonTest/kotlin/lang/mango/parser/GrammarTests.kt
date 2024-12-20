package lang.mango.parser

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GrammarTests {

    private fun createBody(input: String): AST.FunctionDeclaration {
        val main = """
            fn main() {
                $input
            }
        """.trimIndent()
        val program = Grammar.parseToEnd(main) as AST.Program
        assertEquals(1, program.statements.size)
        return program.statements[0] as AST.FunctionDeclaration
    }

    @Test
    fun testEmpty () {
        val input = ""

        val result = Grammar.parseToEnd(input)

        assertIs<AST.Program>(result)
        assertEquals(0, result.statements.size)
    }

    @Test
    fun emptyMain() {
        val input = """
            fn main() {
            }
        """.trimIndent()

        val result = Grammar.parseToEnd(input) as AST.Program

        assertEquals(1, result.statements.size)
        val statement = result.statements[0]

        assertIs<AST.FunctionDeclaration>(statement)
        assertEquals("main", statement.identifier.name)
        assertEquals(0, statement.parameters.size)
        assertEquals(0, statement.body.size)
    }

    @Test
    fun assignment() {
        val function = createBody("""
            let a = 1
            let b = 2
        """.trimIndent())

        assertEquals(2, function.body.size)

        val a = function.body[0] as AST.ValueDeclaration
        assertEquals("a", a.identifier.name)
        assertIs<AST.Constant.Integer>(a.expression)
        assertEquals(1, a.expression.value)

        val b = function.body[1] as AST.ValueDeclaration
        assertEquals("b", b.identifier.name)
        assertIs<AST.Constant.Integer>(b.expression)
        assertEquals(2, b.expression.value)
    }

    @Test
    fun expressionAssignment() {
        val function = createBody("""
            let a = 1 + 2
        """.trimIndent())

        assertEquals(1, function.body.size)

        val a = function.body[0] as AST.ValueDeclaration
        assertEquals("a", a.identifier.name)
        assertIs<AST.BinaryOperation>(a.expression)
        val binary = a.expression
        assertIs<AST.Constant.Integer>(binary.left)
        assertEquals(1, binary.left.value)
        assertIs<AST.Constant.Integer>(binary.right)
        assertEquals(2, binary.right.value)
    }

    @Test
    fun functionCall() {
        val main = createBody("""
            let a = add(1, 2)
        """.trimIndent())

        assertEquals(1, main.body.size)

        val a = main.body[0] as AST.ValueDeclaration
        assertEquals("a", a.identifier.name)
        assertIs<AST.FunctionCall>(a.expression)

        val call = a.expression
        assertEquals("add", call.identifier.name)
        assertEquals(2, call.arguments.size)
        assertIs<AST.Constant.Integer>(call.arguments[0])
        assertEquals(1, (call.arguments[0] as AST.Constant.Integer).value)
        assertIs<AST.Constant.Integer>(call.arguments[1])
        assertEquals(2, (call.arguments[1] as AST.Constant.Integer).value)
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
    }
}