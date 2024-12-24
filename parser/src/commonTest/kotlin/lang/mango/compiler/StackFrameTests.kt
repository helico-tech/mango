package lang.mango.compiler

import lang.mango.parser.AST
import lang.mango.parser.MangoParser
import kotlin.test.*

class StackFrameTests {

    private fun create(input: String): AST.Declaration.Function {
        val program = MangoParser.parse(input)
        return program.functions.first()
    }

    @Test
    fun empty() {
        val function = create("fn test() {}")

        val descriptor = StackFrame(function)

        assertEquals(2, descriptor.totalSize)
    }

    @Test
    fun onlyArguments() {
        val function = create("fn test(a, b) {}")

        val descriptor = StackFrame(function)

        assertEquals(4, descriptor.totalSize)

        assertEquals(3, descriptor.offset(StackFrame.Data.ReturnValue))
        assertEquals(2, descriptor.offset(StackFrame.Data.ReturnAddress))
        assertEquals(1, descriptor.offset(StackFrame.Data.Local("a")))
        assertEquals(0, descriptor.offset(StackFrame.Data.Local("b")))
    }

    @Test
    fun onlyLocals() {
        val function = create("""
            fn test() {
                let a = 1
                let b = 2
            }
        """.trimIndent())

        val descriptor = StackFrame(function)

        assertEquals(4, descriptor.totalSize)

        assertEquals(3, descriptor.offset(StackFrame.Data.ReturnValue))
        assertEquals(2, descriptor.offset(StackFrame.Data.ReturnAddress))
        assertEquals(1, descriptor.offset(StackFrame.Data.Local("a")))
        assertEquals(0, descriptor.offset(StackFrame.Data.Local("b")))
    }
}