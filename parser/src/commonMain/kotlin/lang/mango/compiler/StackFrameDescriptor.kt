package lang.mango.compiler

import lang.mango.parser.AST
import lang.mango.parser.BaseVisitor

class StackFrameDescriptor(val function: AST.Declaration.Function) {

    sealed interface Data {
        data object ReturnAddress : Data
        data object ReturnValue : Data
        data class Local(val name: String) : Data
        data object RuntimeValue : Data
    }

    private val stack = ArrayDeque<Data>()

    val totalSize: Int get() = stack.size

    val returnSize = 2

    val localsSize get() = stack.count { it is Data.Local }

    init {
        stack.addFirst(Data.ReturnValue)
        stack.addFirst(Data.ReturnAddress)

        function.arguments.forEach { stack.addFirst(Data.Local(it.name)) }

        val localsVisitor = LocalsVisitor()
        localsVisitor.visit(function)

        localsVisitor.locals.forEach { stack.addFirst(Data.Local(it.identifier.name)) }
    }

    fun push() {
        stack.addFirst(Data.RuntimeValue)
    }

    fun pop() {
        if (stack.first() !is Data.RuntimeValue) {
            throw IllegalStateException("Can not pop non-runtime value")
        }
        stack.removeFirst()
    }

    fun offset(data: Data): Int {
        val offset = stack.indexOf(data)
        if (offset == -1) {
            throw IllegalArgumentException("Unknown data: $data")
        }
        return offset
    }
}

class LocalsVisitor : BaseVisitor() {

    private val _locals = mutableListOf<AST.Declaration.Variable>()

    val locals: List<AST.Declaration.Variable> get() = _locals

    override fun functionDeclaration(functionDeclaration: AST.Declaration.Function) {
        _locals.clear()
        super.functionDeclaration(functionDeclaration)
    }

    override fun variableDeclaration(ast: AST.Declaration.Variable) {
        _locals.add(ast)
    }
}
