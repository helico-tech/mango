package lang.mango.compiler

class VirtualMachine(
    val instructions: List<ASM>,
) {

    val stack = ArrayDeque<Int>()

    var ip = 0

    fun run() {
        while (ip < instructions.size) {
            step()
        }
    }

    fun step() {
        if (ip >= instructions.size) {
            return
        }

        handle(instructions[ip])
    }

    private fun pop() = stack.removeFirst()

    private fun handle(instruction: ASM) {
        ip++
        when (instruction) {
            is ASM.Load.Constant -> stack.addFirst(instruction.value)
            is ASM.Load.Relative -> stack.addFirst(stack[instruction.offset])
            is ASM.Load.Label -> error("Can not load from label, need to be linked")
            is ASM.Store -> {
                val value = pop()
                stack[instruction.offset] = value
            }

            is ASM.Jump -> {
                ip = pop()
            }

            is ASM.Pop -> {
                repeat(instruction.count) {
                    pop()
                }
            }

            is ASM.Exit -> ip = instructions.size
            else -> error("Unknown instruction: $instruction")
        }
    }
}