package lang.mango.compiler

class MangoLinker(
    val encoder: Encoder
) {
    fun link(chunks: List<ASM.Chunk>): List<ASM> {
        val instructions = chunks.flatMap { chunk ->
            chunk.instructions.mapIndexed { index, annotated ->
                if (index == 0) {
                    annotated.copy(labels = listOf(chunk.name) + annotated.labels)
                } else {
                    annotated
                }
            }
        }

        val labelMap = mutableMapOf<String, Int>()

        var ip = 0

        instructions.forEach { annotated ->
            annotated.labels.forEach { label ->
                labelMap[label] = ip
            }

            ip += encoder.sizeOf(annotated.instruction)
        }

        return instructions.map { annotated ->
            val instruction = annotated.instruction

            when (instruction) {
                is ASM.Load.Label -> {
                    val offset = labelMap[instruction.label] ?: error("Label not found: ${instruction.label}")
                    ASM.Load.Constant(offset)
                }
                else -> instruction
            }
        }
    }
}