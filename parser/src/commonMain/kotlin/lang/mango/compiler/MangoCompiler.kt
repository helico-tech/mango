package lang.mango.compiler

import lang.mango.parser.AST

object MangoCompiler {

    fun compile(program: AST.Program): List<Chunk> {
        return program.functions.map {
            val functionCompiler = FunctionCompiler(it)
            functionCompiler.compile()
        }
    }
}

class FunctionCompiler(val function: AST.Declaration.Function) {

    val stackFrame = StackFrame(function)

    fun compile(): Chunk {
        val instructions = block(function.body)
        return Chunk(function.identifier.name, instructions)
    }

    fun statement(statement: AST.Statement): List<Instruction> {
        return when (statement) {
            is AST.Control -> control(statement)
            is AST.Declaration -> declaration(statement)
            is AST.Expression -> expression(statement)
            else -> throw UnsupportedOperationException("Unknown statement: $statement")
        }
    }

    fun declaration(declaration: AST.Declaration): List<Instruction> {
        return when (declaration) {
            is AST.Declaration.Variable -> {
                val statements = expression(declaration.expression)
                val store = storeLocal(declaration.identifier.name)
                return statements + store
            }
            else -> throw UnsupportedOperationException("Unknown declaration: $declaration")
        }
    }

    fun expression(expression: AST.Expression): List<Instruction> {
        return when (expression) {
            is AST.Identifier -> listOf(loadLocal(expression.name))
            is AST.Literal.Integer -> listOf(Push(expression.value))
            is AST.BinaryOperation -> {
                val left = expression(expression.left)
                val right = expression(expression.right)
                right + left + Arithmetic(expression.operator)
            }
            else -> throw UnsupportedOperationException("Unknown expression: $expression")
        }
    }

    fun control(control: AST.Control): List<Instruction> {
        return when (control) {
            is AST.Control.Return -> returnStatement(control)
            else -> throw UnsupportedOperationException("Unknown control: $control")
        }
    }

    fun block(block: AST.Block): List<Instruction> {
        return block.statements.flatMap { statement(it) }
    }

    private fun returnStatement(control: AST.Control.Return): List<Instruction> {
        // place result on stack
        val expression = expression(control.expression)

        // place result in return slot
        val offset = stackFrame.offset(StackFrame.Data.ReturnValue)
        val store = Store("__return__", offset)

        // remove stack frame contents and jump to return address
        val pop = Pop(stackFrame.totalSize - stackFrame.returnSize)

        return expression + store + pop + Jump
    }

    private fun storeLocal(name: String): Store {
        val offset = stackFrame.offset(StackFrame.Data.Local(name))
        return Store(name, offset)
    }

    private fun loadLocal(name: String): Load {
        val offset = stackFrame.offset(StackFrame.Data.Local(name))
        return Load(name, offset)
    }
}