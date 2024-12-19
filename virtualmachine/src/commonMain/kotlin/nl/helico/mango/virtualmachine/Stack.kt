package nl.helico.mango.virtualmachine

interface Stack {
    fun get(offset: Int): Int
    fun push(value: Int)
    fun pop(): Int

    fun peek(): Int = get(0)
}

class ArrayStack : Stack {
    private val stack = ArrayDeque<Int>()

    override fun get(offset: Int): Int {
        return stack[stack.size - 1 - offset]
    }

    override fun push(value: Int) {
        stack.add(value)
    }

    override fun pop(): Int {
        return stack.removeLast()
    }
}

fun Stack(): Stack = ArrayStack()