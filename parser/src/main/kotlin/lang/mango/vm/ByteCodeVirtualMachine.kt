package lang.mango.vm

import java.nio.ByteBuffer

class ByteCodeVirtualMachine(
    val buffer: ByteArray
) {
    val stack = ArrayDeque<Int>()

    var ip = 0

    fun run() {
        while (ip < buffer.size) {
            step()
        }
    }

    fun step() {
        if (ip >= buffer.size) {
            return
        }

        handle(buffer[ip])
    }

    private fun handle(byte: Byte) {
        ip++
        when (byte.toInt()) {
            0x00 -> ip = buffer.size
            0x01 -> ip = pop()
            0x02 -> {
                val address = pop()
                if (pop() == 0) {
                    ip = address
                }
            }

            0x10 -> {
                stack.addFirst(ByteBuffer.wrap(buffer, ip, 4).int)
                ip += 4
            }
            0x11 -> {
                val offset = ByteBuffer.wrap(buffer, ip, 4).int
                stack.addFirst(stack[offset])
                ip += 4
            }

            0x20 -> {
                val offset = ByteBuffer.wrap(buffer, ip, 4).int
                val value = pop()
                stack[offset] = value
                ip += 4
            }

            0x21 -> {
                repeat(ByteBuffer.wrap(buffer, ip, 4).int) {
                    pop()
                }
                ip += 4
            }

            in  0x30 .. 0x34 -> {
                val left = pop()
                val right = pop()
                val result = when (byte.toInt()) {
                    0x30 -> left + right
                    0x31 -> left - right
                    0x32 -> left * right
                    0x33 -> left / right
                    0x34 -> left % right
                    else -> error("Unknown operation: $byte")
                }
                stack.addFirst(result)
            }

            in 0x40 .. 0x44 -> {
                val left = pop()
                val right = pop()
                val result = when (byte.toInt()) {
                    0x40 -> if (left == right) 1 else 0
                    0x41 -> if (left > right) 1 else 0
                    0x42 -> if (left < right) 1 else 0
                    0x43 -> if (left >= right) 1 else 0
                    0x44 -> if (left <= right) 1 else 0
                    else -> error("Unknown operation: $byte")
                }
                stack.addFirst(result)
            }

            else -> error("Unknown operation: $byte")
        }
    }

    private fun pop() = stack.removeFirst()
}