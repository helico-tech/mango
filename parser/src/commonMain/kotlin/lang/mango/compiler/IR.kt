package lang.mango.compiler

class Chunk(val name: String, val stackFrameDescriptor: StackFrameDescriptor, val instructions: List<Instruction>)

sealed interface Instruction

// load from local or constant
sealed interface Load : Instruction {
    data class Local(val name: String): Load
    data class Constant(val value: Int): Load
}

// store to local
data class Store(val name: String): Instruction

// arithmetic operation
data class Arithmetic(val operator: String): Instruction

// control
data object Return: Instruction

