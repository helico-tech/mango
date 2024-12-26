package lang.mango.compiler

import lang.mango.parser.AST

object MangoCompiler {
    fun compile(program: AST.Program, bootstrap: Boolean = true): List<ASM.Chunk> {
        val functionResolver = FunctionResolver(program)

        val chunks = mutableListOf<ASM.Chunk>()

        if (bootstrap) {
            val bootstrapCompiler = BootstrapCompiler(functionResolver)
            chunks.add(bootstrapCompiler.compile())
        }

        chunks.addAll(program.functions.map { FunctionCompiler(it, functionResolver) }.map(FunctionCompiler::compile))

        return chunks
    }
}

fun interface FunctionResolver {
    fun resolve(name: String): AST.Declaration.Function?

    companion object {
        operator fun invoke(program: AST.Program): FunctionResolver {
            val functions = program.functions.associateBy { it.identifier.name }
            return FunctionResolver { functions[it] }
        }
    }
}

abstract class AbstractCompiler(
    protected val functionResolver: FunctionResolver
) {
    protected val labels = mutableMapOf<Int, MutableList<String>>()
    protected val comments = mutableMapOf<Int, String>()
    protected val instructions = ArrayDeque<ASM>()

    val annotated get() = instructions.mapIndexed { lineNo, ir -> ASM.Annotated(ir, labels[lineNo] ?: emptyList(), comments[lineNo]) }

    abstract fun compile(): ASM.Chunk

    protected open fun emit(ASM: ASM, labels: List<String> = emptyList(), comment: String? = null) {
        instructions.addLast(ASM)
        if (labels.isNotEmpty()) label(labels)
        if (comment != null) comment(comment)
    }

    protected fun label(_labels: List<String>, offset: Int? = null) {
        labels[instructions.size - 1 + (offset ?: 0)] = _labels.toMutableList()
    }

    protected fun comment(comment: String, offset: Int? = null) {
        comments[instructions.size - 1 + (offset ?: 0)] = comment
    }

    protected fun functionCall(call: AST.FunctionCall, returnAddressLabel: String) {
        val function = requireNotNull(functionResolver.resolve(call.identifier.name)) { "Unknown function: ${call.identifier.name}" }
        val stackFrameDescriptor = StackFrameDescriptor(function)

        emit(ASM = ASM.Load.Constant(0), comment = "Call [${call.identifier.name}] push return value")
        emit(ASM = ASM.Load.Label(returnAddressLabel), comment = "Call [${call.identifier.name}] return address")

        // locals - arguments first
        call.arguments.forEachIndexed { index, it ->
            expression(it, offset = 2 + index)
        }

        // locals - block scoped
        repeat(stackFrameDescriptor.localsSize - call.arguments.size) {
            emit(ASM = ASM.Load.Constant(0))
        }

        // push the address
        emit(ASM = ASM.Load.Label(call.identifier.name), comment = "Call [${call.identifier.name}] address")
        emit(ASM = ASM.Jump, comment = "Call [${call.identifier.name}] jump")
    }

    protected abstract fun expression(expression: AST.Expression, offset: Int = 0)
}

class BootstrapCompiler(
    functionResolver: FunctionResolver
) : AbstractCompiler(functionResolver) {
    override fun compile(): ASM.Chunk {
        val call = AST.FunctionCall(AST.Identifier("main"), emptyList())
        functionCall(call, ".exit")
        emit(ASM.Exit, comment = "Exit", labels = listOf(".exit"))
        return ASM.Chunk.Raw("bootstrap", annotated)
    }

    override fun expression(expression: AST.Expression, offset: Int) {
        throw UnsupportedOperationException("Not supported in bootstrap compiler")
    }
}

class FunctionCompiler(
    private val function: AST.Declaration.Function,
    functionResolver: FunctionResolver
) : AbstractCompiler(functionResolver) {

    private var returnLabelCounter = 0
    private val returnLabelStack = ArrayDeque<String>()
    private var labelsToAdd = ArrayDeque<String>()
    private val stackFrameDescriptor = StackFrameDescriptor(function)

    override fun compile() : ASM.Chunk.Function {
        block(function.body)
        return ASM.Chunk.Function(function, annotated)
    }

    private fun block(block: AST.Block) {
        block.statements.forEach(::statement)
    }

    private fun statement(statement: AST.Statement) {
        when (statement) {
            is AST.Control.Return -> {
                // push return value on the stack
                expression(statement.expression)

                // load return value into the return register
                val offset = stackFrameDescriptor.offset(StackFrameDescriptor.Data.ReturnValue)
                emit(ASM.Store(offset), comment = "Return value")

                returnFromFunction()
            }

            is AST.Declaration.Variable -> {
                expression(statement.expression)
                val offset = stackFrameDescriptor.offset(StackFrameDescriptor.Data.Local(statement.identifier.name))
                emit(ASM.Store(offset), comment = "Variable [${statement.identifier.name}]")
            }

            is AST.Control.When -> {
                expression(statement.expression)
                emit(ASM.Load.Label(pushReturnLabel()))
                emit(ASM.JumpWhenZero)
                block(statement.body)
                requireReturnLabel()
            }

            else -> throw IllegalArgumentException("Unknown statement: $statement")
        }
    }

    override fun expression(expression: AST.Expression, offset: Int) {
        when (expression) {
            is AST.Identifier -> {
                val offset = stackFrameDescriptor.offset(StackFrameDescriptor.Data.Local(expression.name)) + offset
                emit(ASM.Load.Relative(offset), comment = "Variable [${expression.name}]")
            }
            is AST.Literal.Integer -> {
                emit(ASM.Load.Constant(expression.value))
            }
            is AST.BinaryOperation -> {
                expression(expression.right, offset)
                stackFrameDescriptor.push()
                expression(expression.left, offset)
                stackFrameDescriptor.push()

                val op = when (expression.operator) {
                    "plus" -> ASM.Op.Add
                    "minus" -> ASM.Op.Sub
                    "times" -> ASM.Op.Mul
                    "divide" -> ASM.Op.Div
                    "modulo" -> ASM.Op.Mod
                    "greaterThan" -> ASM.Op.GreaterThan
                    "lessThan" -> ASM.Op.LessThan
                    "greaterThanOrEqual" -> ASM.Op.GreaterThanOrEqual
                    "lessThanOrEqual" -> ASM.Op.LessThanOrEqual
                    "doubleEqual" -> ASM.Op.Equal
                    else -> throw IllegalArgumentException("Unknown operator: ${expression.operator}")
                }
                emit(op)
                stackFrameDescriptor.pop()
                stackFrameDescriptor.pop()
            }

            is AST.FunctionCall -> {
                functionCall(expression, pushReturnLabel())
                requireReturnLabel()
            }

            else -> throw IllegalArgumentException("Unknown expression: $expression")
        }
    }

    override fun emit(ASM: ASM, labels: List<String>, comment: String?) {
        if (labelsToAdd.isNotEmpty()) {
            val labels = mutableListOf<String>().also { it.addAll(labelsToAdd)  }
            labelsToAdd.clear()
            super.emit(ASM, labels, comment)
        } else {
            super.emit(ASM, labels, comment)
        }
    }

    private fun returnFromFunction() {
        // unwind the stack
        if (stackFrameDescriptor.localsSize > 0) {
            emit(ASM.Pop(stackFrameDescriptor.localsSize), comment = "Unwind stack")
        }

        // jump to return address
        emit(ASM.Jump, comment = "Return")
    }

    private fun pushReturnLabel(): String {
        val label = ".return.${function.identifier.name}.${returnLabelCounter++}"
        returnLabelStack.addFirst(label)
        return label
    }

    private fun popReturnLabel(): String {
        return returnLabelStack.removeFirst()
    }

    private fun requireReturnLabel() {
        labelsToAdd.addFirst(popReturnLabel())
    }
}