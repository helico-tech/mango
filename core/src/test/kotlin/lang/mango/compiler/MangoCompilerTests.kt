package lang.mango.compiler

import lang.mango.parser.AST
import lang.mango.parser.MangoParser
import lang.mango.vm.ASMVirtualMachine
import kotlin.test.Test
import kotlin.test.assertEquals

class MangoCompilerTests {

    private fun create(source: String, bootstrap: Boolean = true): Pair<List<ASM.Chunk>, List<ASM>> {
        val ast = MangoParser.parse(source)
        val chunks = MangoCompiler.compile(ast, bootstrap = bootstrap)
        val linked = MangoLinker(NullEncoder).link(chunks)
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
        assertEquals(3, chunk.instructions.size)
        assertEquals(ASM.Load.Constant(0), chunk.instructions[0].instruction)
        assertEquals(ASM.Store(1), chunk.instructions[1].instruction)
        assertEquals(ASM.Jump, chunk.instructions[2].instruction)
    }

    @Test
    fun emptyMainWithBootstrap() {
        val (result, linked) = create("""
            fn main() {}
        """.trimIndent())

        assertEquals(2, result.size)

        val pretty = result.toPrettyString()

        assertEquals(8, linked.size)
        assertEquals(ASM.Load.Constant(0), linked[0])
        assertEquals(ASM.Load.Constant(4), linked[1])
        assertEquals(ASM.Load.Constant(5), linked[2])
        assertEquals(ASM.Jump, linked[3])
        assertEquals(ASM.Exit, linked[4])
        assertEquals(ASM.Load.Constant(0), linked[5])
        assertEquals(ASM.Store(1), linked[6])
        assertEquals(ASM.Jump, linked[7])

        val vm = ASMVirtualMachine(linked)
        vm.run()
        assertEquals(8, vm.ip)
        assertEquals(0, vm.stack.first())
        assertEquals(1, vm.stack.size)
    }

    @Test
    fun simpleReturn() {
        val (_, linked) = create("""
            fn main() {
                return 42
            }
        """.trimIndent())

        val vm = ASMVirtualMachine(linked)
        vm.run()
        assertEquals(42, vm.stack.first())
        assertEquals(1, vm.stack.size)
    }

    @Test
    fun variables() {
        val (_, linked) = create("""
            fn main() {
                let a = 42
                return a
            }
        """.trimIndent())

        val vm = ASMVirtualMachine(linked)
        vm.run()
        assertEquals(42, vm.stack.first())
        assertEquals(1, vm.stack.size)
    }

    @Test
    fun sumVariables() {
        val (result, linked) = create("""
            fn main() {
                let a = 42
                let b = 8
                return a + b + 10
            }
        """.trimIndent())

        val pretty = result.toPrettyString()

        val vm = ASMVirtualMachine(linked)
        vm.run()

        assertEquals(60, vm.stack.first())
        assertEquals(1, vm.stack.size)
    }

    @Test
    fun returnFunctionCall() {
        val (result, linked) = create("""
            fn foo() {
                return 42
            }
            
            fn main() {
                return foo()
            }
        """.trimIndent())

        val pretty = result.toPrettyString()

        val vm = ASMVirtualMachine(linked)
        vm.run()

        assertEquals(42, vm.stack.first())
        assertEquals(1, vm.stack.size)
    }

    @Test
    fun additionTest() {
        val (result, linked) = create("""
            fn add(a, b) {
                return a + b
            }
            
            fn main() { 
                let a = 4
                let b = 5
                return add(a, b)
            }
        """.trimIndent())

        val pretty = result.toPrettyString()

        val vm = ASMVirtualMachine(linked)
        vm.run()

        assertEquals(9, vm.stack.first())
    }

    @Test
    fun conditional() {
        val (result, linked) = create("""
            fn main() {               
                when (5 > 4) {
                    return 3
                }
                return 2
            }
        """.trimIndent())

        val pretty = result.toPrettyString()

        val vm = ASMVirtualMachine(linked)
        vm.run()

        assertEquals(3, vm.stack.first())
    }

    @Test
    fun conditional2() {
        val (result, linked) = create("""
            fn main() {               
                when (5 < 4) {
                    return 3
                }
                return 2
            }
        """.trimIndent())

        val pretty = result.toPrettyString()

        val vm = ASMVirtualMachine(linked)
        vm.run()

        assertEquals(2, vm.stack.first())
    }

    @Test
    fun conditionals() {
        val (result, linked) = create("""
            fn main() {
                when (5 > 4) {
                    when (3 == 3) return 3
                }
                return 2
            }
        """.trimIndent())

        val pretty = result.toPrettyString()

        val vm = ASMVirtualMachine(linked)
        vm.run()

        assertEquals(3, vm.stack.first())
    }

    @Test
    fun recursion() {
        val (result, linked) = create("""
            fn foo(n) {
                when (n == 0) return 0
                return foo(n - 1)
            }
            
            fn main() {
                return foo(2)
            }
        """.trimIndent())

        val pretty = result.toPrettyString()

        val vm = ASMVirtualMachine(linked)
        vm.run()

        assertEquals(0, vm.stack.first())
    }

    @Test
    fun fibonacci() {
        val (result, linked) = create("""
            fn fib(n) {
                when (n == 0) return 0
                when (n == 1) return 1

                return fib(n - 1) + fib(n - 2)
            }

            fn main() {
                return fib(30)
            }
        """.trimIndent())

        val pretty = result.toPrettyString()

        val vm = ASMVirtualMachine(linked)
        vm.run()

        assertEquals(832040, vm.stack.first())
        assertEquals(1, vm.stack.size)
    }
}