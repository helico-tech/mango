package lang.mango.parser

class NestedFunctionValidatorVisitor : BaseVisitor() {
    private val functionStack = mutableListOf<String>()

    override fun functionDeclaration(functionDeclaration: AST.Declaration.Function) {
        if (functionStack.isNotEmpty()) {
            throw IllegalStateException("Nested functions are not allowed")
        }
        functionStack.add(functionDeclaration.identifier.name)
        super.functionDeclaration(functionDeclaration)
        functionStack.removeAt(functionStack.size - 1)
    }
}