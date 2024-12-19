package nl.helico.mango.instructionset

sealed interface Instruction

// stack operations
data class Push(val value: Int) : Instruction
data object Pop : Instruction
data object Swap : Instruction

// arithmetic operations
data object Add : Instruction
data object Subtract : Instruction

// program operations
data object Halt : Instruction