package lang.mango.compiler

import lang.mango.parser.AST

class MangoCompiler {

    fun compile(program: AST.Program): List<Chunk> {
        return program.functions.map { function(it) }
    }

    fun function(function: AST.Declaration.Function): Chunk {
        val instructions = function.body.statements.flatMap { statement(it) }
        val stackFrameDescriptor = StackFrameDescriptor(function)
        return Chunk(function.identifier.name, stackFrameDescriptor, instructions)
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
                val statements = statement(declaration.expression)
                return statements + Store(declaration.identifier.name)
            }
            else -> throw UnsupportedOperationException("Unknown declaration: $declaration")
        }
    }

    fun expression(expression: AST.Expression): List<Instruction> {
        return when (expression) {
            is AST.Identifier -> listOf(Load.Local(expression.name))
            is AST.Literal.Integer -> listOf(Load.Constant(expression.value))
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
            is AST.Control.Return -> expression(control.expression) + Return
            else -> throw UnsupportedOperationException("Unknown control: $control")
        }
    }
}