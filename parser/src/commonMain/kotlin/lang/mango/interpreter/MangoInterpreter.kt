package lang.mango.interpreter

import lang.mango.parser.AST
import lang.mango.parser.Tokens

class MangoInterpreter(
    private val runtimeEnvironment: RuntimeEnvironment = RuntimeEnvironment()
) {
    fun interpret(program: AST.Program): Int {
        program(program)

        val mainCall = AST.FunctionCall(AST.Identifier("main"), emptyList())

        return functionCall(mainCall)
    }

    private fun program(program: AST.Program) {
        program.functions.forEach { function ->
            runtimeEnvironment.registerFunction(function)
        }
    }

    private fun functionCall(functionCall: AST.FunctionCall): Int {
        val function = runtimeEnvironment.getFunction(functionCall.identifier.name)

        val variables = function.parameters.mapIndexed { index, parameter ->
            val value = functionCall.arguments[index]
            parameter.name to expression(value)
        }.toMap()

        runtimeEnvironment.pushFrame()

        variables.forEach { (name, value) ->
            runtimeEnvironment.setVariable(name, value)
        }

        try {
            block(function.body)
        } catch (e: ReturnException) {
            runtimeEnvironment.popFrame()
            return e.value
        }

        runtimeEnvironment.popFrame()
        return 0
    }

    private fun block(block: AST.Block) {
        block.statements.forEach { statement ->
            statement(statement)
        }
    }

    private fun statement(statement: AST.Statement) {
        when (statement) {
            is AST.Expression -> expression(statement)
            is AST.Assignment -> assignment(statement)
            is AST.Declaration -> declaration(statement)
            is AST.Control -> control(statement)
            is AST.Block -> block(statement)
        }
    }

    private fun assignment(assignment: AST.Assignment) {
        val value = expression(assignment.expression)
        runtimeEnvironment.setVariable(assignment.identifier.name, value)
    }

    private fun declaration(declaration: AST.Declaration) {
        when (declaration) {
            is AST.Declaration.Variable -> variableDeclaration(declaration)
            is AST.Declaration.Function -> throw RuntimeException("Function declarations are not allowed inside functions")
        }
    }

    private fun variableDeclaration(declaration: AST.Declaration.Variable) {
        val value = expression(declaration.expression)
        runtimeEnvironment.setVariable(declaration.identifier.name, value)
    }

    private fun control(control: AST.Control) {
        when (control) {
            is AST.Control.When -> controlWhen(control)
            is AST.Control.While -> controlWhile(control)
            is AST.Control.Return -> controlReturn(control)
        }
    }

    private fun controlWhen(control: AST.Control.When) {
        val value = expression(control.expression)
        if (value != 0) {
            block(control.body)
        }
    }

    private fun controlWhile(control: AST.Control.While) {
        while (expression(control.condition) != 0) {
            block(control.body)
        }
    }

    private fun controlReturn(control: AST.Control.Return) {
        val value = expression(control.expression)
        throw ReturnException(value)
    }

    private fun expression(expression: AST.Expression): Int {
        return when (expression) {
            is AST.Literal.Integer -> expression.value
            is AST.UnaryOperation -> unaryOperation(expression)
            is AST.BinaryOperation -> binaryOperation(expression)
            is AST.Identifier -> identifier(expression)
            is AST.FunctionCall -> functionCall(expression)
        }
    }

    private fun unaryOperation(unaryOperation: AST.UnaryOperation): Int {
        val value = expression(unaryOperation.expression)
        return when (unaryOperation.operator) {
            "-" -> -value
            else -> throw RuntimeException("Unknown unary operator ${unaryOperation.operator}")
        }
    }

    private fun binaryOperation(binaryOperation: AST.BinaryOperation): Int {
        val left = expression(binaryOperation.left)
        val right = expression(binaryOperation.right)
        return when (binaryOperation.operator) {
            Tokens.plus.name -> left + right
            Tokens.minus.name -> left - right
            Tokens.multiply.name -> left * right
            Tokens.divide.name -> left / right
            Tokens.doubleEqual.name -> if (left == right) 1 else 0
            Tokens.notEqual.name -> if (left != right) 1 else 0
            else -> throw RuntimeException("Unknown binary operator ${binaryOperation.operator}")
        }
    }

    private fun identifier(identifier: AST.Identifier): Int {
        return runtimeEnvironment.getVariable(identifier.name)
    }

    class ReturnException(val value: Int): RuntimeException()
}