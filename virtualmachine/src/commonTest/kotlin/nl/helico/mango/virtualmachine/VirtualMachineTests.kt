package nl.helico.mango.virtualmachine

import nl.helico.mango.instructionset.Program
import kotlin.test.Test
import kotlin.test.assertEquals

class VirtualMachineTests {

    @Test
    fun addition() {
        val program = Program(
            listOf(
                Push(1),
                Push(2),
                Add,
                Halt
            )
        )
        val vm = VirtualMachine(program)

        vm.execute()

        assertEquals(3, vm.stack.peek())
    }
}