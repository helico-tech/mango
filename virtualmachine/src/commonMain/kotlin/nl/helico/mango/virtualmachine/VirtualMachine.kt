package nl.helico.mango.virtualmachine

import nl.helico.mango.instructionset.Program

class VirtualMachine(
    val program: Program,
    val stack: Stack = Stack()
) {
    private var instructionCounter = 0

    fun canContinue(): Boolean {
        return program[instructionCounter] != null
    }

    fun step() {
        val instruction = program[instructionCounter] ?: return
        instructionCounter++

        when (instruction) {
            is Push -> stack.push(instruction.value)
            is Pop -> stack.pop()
            is Swap -> {
                val a = stack.pop()
                val b = stack.pop()
                stack.push(a)
                stack.push(b)
            }
            is Add -> {
                val a = stack.pop()
                val b = stack.pop()
                stack.push(a + b)
            }
            is Subtract -> {
                val a = stack.pop()
                val b = stack.pop()
                stack.push(a - b)
            }
            is Halt -> instructionCounter = program.size
        }
    }

    fun execute() {
        while (canContinue()) {
            step()
        }
    }
}