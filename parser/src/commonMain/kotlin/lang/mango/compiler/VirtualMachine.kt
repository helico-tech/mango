package lang.mango.compiler

class VirtualMachine(val program: List<Instruction>) {

    val stack = ArrayDeque<Int>()

    var ip = 0
        private set

    fun run() {
        while (ip < program.size) {
            step()
        }
    }

    fun step() {
        require(ip < program.size) { "Program terminated" }

        handle(program[ip ++])
    }

    private fun handle(instruction: Instruction) {
        when (instruction) {
            is Push -> {
                stack.addFirst(instruction.value)
            }
            is Pop -> {
                repeat(instruction.count) {
                    stack.removeFirst()
                }
            }

            is Load -> {
                stack.addFirst(stack[instruction.offset])
            }

            is Store -> {
                stack[instruction.offset] = stack.removeFirst()
            }

            is Arithmetic -> {
                val a = stack.removeFirst()
                val b = stack.removeFirst()
                val result = when (instruction.operator) {
                    "plus" -> a + b
                    "minus" -> a - b
                    "times" -> a * b
                    "divide" -> a / b
                    else -> throw UnsupportedOperationException("Unknown operator: ${instruction.operator}")
                }
                stack.addFirst(result)
            }

            Jump -> {
                ip = stack.removeFirst()
            }
        }
    }
}