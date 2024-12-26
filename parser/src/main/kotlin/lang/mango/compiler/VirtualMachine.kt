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

            is ASM.JumpWhenZero -> {
                val address = pop()
                if (pop() == 0) {
                    ip = address
                }
            }

            is ASM.Pop -> {
                repeat(instruction.count) {
                    pop()
                }
            }

            is ASM.Exit -> ip = instructions.size

            is ASM.Op -> {
                val left = pop()
                val right = pop()
                val result = when (instruction) {
                    is ASM.Op.Add -> left + right
                    is ASM.Op.Sub -> left - right
                    is ASM.Op.Mul -> left * right
                    is ASM.Op.Div -> left / right
                    is ASM.Op.Mod -> left % right
                    is ASM.Op.GreaterThan -> if (left > right) 1 else 0
                    is ASM.Op.LessThan -> if (left < right) 1 else 0
                    is ASM.Op.GreaterThanOrEqual -> if (left >= right) 1 else 0
                    is ASM.Op.LessThanOrEqual -> if (left <= right) 1 else 0
                    is ASM.Op.Equal -> if (left == right) 1 else 0
                    else -> error("Unknown operator: $instruction")
                }

                stack.addFirst(result)
            }

            else -> error("Unknown instruction: $instruction")
        }
    }
}