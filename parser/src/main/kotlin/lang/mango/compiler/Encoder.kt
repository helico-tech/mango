package lang.mango.compiler

import java.nio.ByteBuffer

interface Encoder {
    fun  sizeOf(asm: ASM): Int
}

object NullEncoder : Encoder {
    override fun sizeOf(asm: ASM): Int {
        return 1
    }
}

object ByteEncoder : Encoder {
    override fun sizeOf(asm: ASM): Int {
        return when (asm) {
            is ASM.Load -> 5
            is ASM.Store -> 5
            is ASM.Pop -> 5
            else -> 1
        }
    }

    fun encode(instructions: List<ASM>): ByteBuffer {
        val buffer = ByteBuffer.allocate(instructions.sumOf { sizeOf(it) })
        instructions.forEach { instruction -> encode(instruction, buffer) }
        return buffer
    }

    private fun encode(instruction: ASM, buffer: ByteBuffer) {
        when (instruction) {
            is ASM.Exit -> buffer.put(0x00)
            is ASM.Jump -> buffer.put(0x01)
            is ASM.JumpWhenZero -> buffer.put(0x02)

            is ASM.Load.Constant -> buffer.put(0x10).putInt(instruction.value)
            is ASM.Load.Relative -> buffer.put(0x11).putInt(instruction.offset)
            is ASM.Load.Label -> throw NotImplementedError()

            is ASM.Store -> buffer.put(0x20).putInt(instruction.offset)
            is ASM.Pop -> buffer.put(0x21).putInt(instruction.count)

            is ASM.Op.Add -> buffer.put(0x30)
            is ASM.Op.Sub -> buffer.put(0x31)
            is ASM.Op.Mul -> buffer.put(0x32)
            is ASM.Op.Div -> buffer.put(0x33)
            is ASM.Op.Mod -> buffer.put(0x34)

            is ASM.Op.Equal -> buffer.put(0x40)
            is ASM.Op.GreaterThan -> buffer.put(0x41)
            is ASM.Op.LessThan -> buffer.put(0x42)
            is ASM.Op.GreaterThanOrEqual -> buffer.put(0x43)
            is ASM.Op.LessThanOrEqual -> buffer.put(0x44)
        }
    }
}