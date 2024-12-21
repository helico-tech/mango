package lang.mango.parser

interface ASTVisitor {

    fun visit(ast: AST) {
        when (ast) {
            is AST.Program -> program(ast)
            is AST.Declaration.Variable -> variableDeclaration(ast)
            is AST.Declaration.Function -> functionDeclaration(ast)
            is AST.Control.When -> whenControl(ast)
            is AST.Control.While -> whileControl(ast)
            is AST.Control.Return -> returnControl(ast)
            is AST.Block -> block(ast)
            is AST.Literal.Integer -> integerLiteral(ast)
            is AST.UnaryOperation -> unaryOperation(ast)
            is AST.BinaryOperation -> binaryOperation(ast)
            is AST.FunctionCall -> functionCall(ast)
            is AST.Identifier -> identifier(ast)
            is AST.Assignment -> assignment(ast)

        }
    }

    fun program(program: AST.Program)
    fun variableDeclaration(variableDeclaration: AST.Declaration.Variable)
    fun functionDeclaration(functionDeclaration: AST.Declaration.Function)

    fun whenControl(whenControl: AST.Control.When)
    fun whileControl(whileControl: AST.Control.While)
    fun returnControl(returnControl: AST.Control.Return)

    fun block(block: AST.Block)

    fun integerLiteral(integerLiteral: AST.Literal.Integer)
    fun unaryOperation(unaryOperation: AST.UnaryOperation)
    fun binaryOperation(binaryOperation: AST.BinaryOperation)
    fun functionCall(functionCall: AST.FunctionCall)
    fun identifier(identifier: AST.Identifier)
    fun assignment(assignment: AST.Assignment)
}

abstract class BaseVisitor : ASTVisitor {
    override fun program(program: AST.Program) {
        program.statements.forEach { visit(it) }
    }

    override fun variableDeclaration(variableDeclaration: AST.Declaration.Variable) {
        visit(variableDeclaration.identifier)
        visit(variableDeclaration.expression)
    }

    override fun functionDeclaration(functionDeclaration: AST.Declaration.Function) {
        visit(functionDeclaration.identifier)
        functionDeclaration.parameters.forEach { visit(it) }
        visit(functionDeclaration.body)
    }

    override fun whenControl(whenControl: AST.Control.When) {
        visit(whenControl.expression)
        visit(whenControl.body)
    }

    override fun whileControl(whileControl: AST.Control.While) {
        visit(whileControl.condition)
        visit(whileControl.body)
    }

    override fun returnControl(returnControl: AST.Control.Return) {
        visit(returnControl.expression)
    }

    override fun block(block: AST.Block) {
        block.statements.forEach { visit(it) }
    }

    override fun integerLiteral(integerLiteral: AST.Literal.Integer) {}

    override fun unaryOperation(unaryOperation: AST.UnaryOperation) {
        visit(unaryOperation.expression)
    }

    override fun binaryOperation(binaryOperation: AST.BinaryOperation) {
        visit(binaryOperation.left)
        visit(binaryOperation.right)
    }

    override fun functionCall(functionCall: AST.FunctionCall) {
        visit(functionCall.identifier)
        functionCall.arguments.forEach { visit(it) }
    }

    override fun identifier(identifier: AST.Identifier) {}

    override fun assignment(assignment: AST.Assignment) {
        visit(assignment.identifier)
        visit(assignment.expression)
    }
}