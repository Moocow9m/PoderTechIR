package tech.poder.ir.data.storage.segment

import tech.poder.ir.commands.Command
import tech.poder.ir.data.Label
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Method
import java.util.*

@JvmInline
value class LoopHolder(val block: Segment) : Segment {

    override fun eval(
        method: Method,
        stack: Stack<Type>,
        currentVars: MutableList<Type>,
        currentIndex: Int,
        labels: MutableMap<Int, Label>
    ): Int {

        val loopStack = Stack<Type>()
        val copy = Stack<Type>()

        stack.forEach {
            loopStack.push(it)
        }

        val index = block.eval(method, stack, currentVars, currentIndex, labels)

        check(loopStack.size == copy.size) {
            "Loop stack does not match original!\n\tLoop:\n\t\t${loopStack.joinToString("\n\t\t")}\n\tOriginal:\n\t\t${
                copy.joinToString("\n\t\t")
            }"
        }
        return index
    }

    override fun size(): Int {
        return block.size()
    }

    override fun toBulk(storage: MutableList<Command>) {
        block.toBulk(storage)
    }
}