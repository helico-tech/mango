package lang.mango.compiler

class Chunk(val name: String, val instructions: List<Instruction>)

sealed interface Instruction

// load local and push on top of stack
data class Load(val name: String, val offset: Int): Instruction

// store to local and push on top of stack
data class Store(val name: String, val offset: Int): Instruction

data class Push(val value: Int): Instruction

data class Pop(val count: Int): Instruction

// arithmetic operation
data class Arithmetic(val operator: String): Instruction

// control
data object Return: Instruction

