package lang.mango.parser

sealed interface AST {

    data class Program(val statements: List<Declaration.Function>): AST

    sealed interface Statement : AST

    sealed interface Declaration : Statement {
        data class Variable(val identifier: Identifier, val expression: Expression): Declaration
        data class Function(val identifier: Identifier, val parameters: List<Identifier>, val body: Block): Declaration
    }

    sealed interface Control : Statement {
        data class When(val expression: Expression, val body: Block) : Control
        data class While(val condition: Expression, val body: Block) : Control
        data class Return(val expression: Expression) : Control
    }

    data class Block(val statements: List<Statement>): Statement {
        val size = statements.size
        operator fun get(index: Int) = statements[index]
    }

    sealed interface Expression : Statement

    sealed interface Literal : Expression {
        data class Integer(val value: Int): Literal
    }

    data class UnaryOperation(val operator: String, val expression: Expression): Expression

    data class BinaryOperation(val left: Expression, val operator: String, val right: Expression): Expression

    data class FunctionCall(val identifier: Identifier, val arguments: List<Expression>): Expression

    data class Identifier(val name: String) : Expression

    data class Assignment(val identifier: Identifier, val expression: Expression): Statement
}