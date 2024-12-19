package nl.helico.mango.instructionset

interface Program {
    operator fun get(index: Int): Instruction?
    val size: Int
}

class ArrayProgram(private val instructions: Array<Instruction>) : Program {

    override val size: Int
        get() = instructions.size

    override fun get(index: Int): Instruction? {
        return instructions.getOrNull(index)
    }
}

fun Program(program: List<Instruction>): Program = ArrayProgram(program.toTypedArray())