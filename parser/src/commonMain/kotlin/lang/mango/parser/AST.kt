package lang.mango.parser

sealed interface AST {

    data class Program(val statements: List<Statement>): AST

    sealed interface Statement : AST

    sealed interface Expression : Statement

    sealed interface Declaration : Statement

    sealed interface Constant : Expression {
        data class Integer(val value: Int): Constant
    }

    data class Block(val statements: List<Statement>): Expression {
        val size = statements.size
        operator fun get(index: Int) = statements[index]
    }

    data class UnaryOperation(val operator: String, val expression: Expression): Expression

    data class BinaryOperation(val left: Expression, val operator: String, val right: Expression): Expression

    data class FunctionCall(val identifier: Identifier, val arguments: List<Expression>): Expression

    data class Identifier(val name: String) : Expression

    data class Assignment(val identifier: Identifier, val expression: Expression): Statement

    data class When(val expression: Expression, val body: Block) : Statement

    data class ValueDeclaration(val identifier: Identifier, val expression: Expression): Declaration

    data class FunctionDeclaration(val identifier: Identifier, val parameters: List<Identifier>, val body: Block): Declaration
}