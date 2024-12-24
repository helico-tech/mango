package lang.mango.compiler

import lang.mango.parser.AST

sealed interface IR {
    sealed interface Load : IR {
        data class Constant(val value: Int) : Load {
            override fun toString(): String {
                return "Load.Constant($value)"
            }
        }
        data class Relative(val offset: Int) : Load {
            override fun toString(): String {
                return "Load.Relative($offset)"
            }
        }
        data class Label(val label: String) : Load {
            override fun toString(): String {
                return "Load.Label($label)"
            }
        }
    }

    data class Store(val offset: Int) : IR

    data class Pop(val count: Int) : IR

    data object Jump : IR

    data object Exit : IR

    sealed interface Op : IR {
        data object Add : Op
        data object Sub : Op
        data object Mul : Op
        data object Div : Op
        data object Mod : Op
    }

    data class Annotated(val ir: IR, val label: String? = null, val comment: String? = null)

    sealed interface Chunk {
        val instructions: List<Annotated>
        val name: String

        data class Function(val function: AST.Declaration.Function, override val instructions: List<Annotated>) : Chunk {
            override val name = function.identifier.name
        }

        data class Raw(override val name: String, override val instructions: List<Annotated>) : Chunk
    }
}

fun IR.Annotated.toPrettyString(): String {
    val label = label?.let { "$it:" } ?: ""
    val comment = comment?.let { " ; $it" } ?: ""
    return "$label\t$ir\t$comment"
}

fun IR.Chunk.toPrettyString(
    indexWidth: Int = instructions.size.toString().length,
    nameWidth: Int = instructions.maxOfOrNull { it.label?.length ?: 0 } ?: 0,
    commentWidth: Int = instructions.maxOfOrNull { it.comment?.length ?: 0 } ?: 0,
    irWidth: Int = instructions.maxOfOrNull { it.ir.toString().length } ?: 0,
    indexOffset: Int = 0
): String {
    return buildString {
        appendLine("$name:")
        instructions.forEachIndexed { i, annotated ->
            val index = (i + indexOffset).toString().padStart(indexWidth)
            val label = (annotated.label ?: "").padEnd(nameWidth)
            val ir = annotated.ir.toString().padEnd(irWidth)
            val comment = annotated.comment?.padEnd(commentWidth) ?: ""
            appendLine("\t$index\t$label\t$ir\t$comment")
        }
    }
}

fun Iterable<IR.Chunk>.toPrettyString(): String {
    val indexWidth = maxOfOrNull { it.instructions.size.toString().length } ?: 0
    val nameWidth = maxOfOrNull { it.instructions.maxOfOrNull { it.label?.length ?: 0 } ?: 0 } ?: 0
    val commentWidth = maxOfOrNull { it.instructions.maxOfOrNull { it.comment?.length ?: 0 } ?: 0 } ?: 0
    val irWidth = maxOfOrNull { it.instructions.maxOfOrNull { it.ir.toString().length } ?: 0 } ?: 0

    return buildString {
        var indexOffset = 0
        this@toPrettyString.forEachIndexed { i, chunk ->
            append(chunk.toPrettyString(indexWidth, nameWidth, commentWidth, irWidth, indexOffset))
            indexOffset += chunk.instructions.size
            if (i != lastIndex) appendLine()
        }
    }
}