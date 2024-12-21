package lang.mango.interpreter

import lang.mango.parser.AST

class RuntimeEnvironment {

    data class StackFrame(
        val variables: MutableMap<String, Int> = mutableMapOf()
    )

    private val functions = mutableMapOf<String, AST.Declaration.Function>()

    private val stack = mutableListOf<StackFrame>()

    fun registerFunction(function: AST.Declaration.Function) {
        functions[function.identifier.name] = function
    }

    fun getFunction(name: String): AST.Declaration.Function {
        return functions[name] ?: throw RuntimeException("Function $name not found")
    }

    fun pushFrame() {
        stack.add(StackFrame())
    }

    fun setVariable(name: String, value: Int) {
        stack.last().variables[name] = value
    }

    fun getVariable(name: String): Int {
        return stack.last().variables[name] ?: throw RuntimeException("Variable $name not found")
    }

    fun popFrame() {
        stack.removeAt(stack.size - 1)
    }
}