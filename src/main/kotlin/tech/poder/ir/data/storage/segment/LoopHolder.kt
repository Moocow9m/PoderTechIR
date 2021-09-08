package tech.poder.ir.data.storage.segment

import tech.poder.ir.commands.Command
import tech.poder.ir.data.Label
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.unlinked.UnlinkedMethod
import tech.poder.ir.util.MemorySegmentBuffer
import java.util.*

@JvmInline
value class LoopHolder(val block: Segment) : Segment {

    override fun eval(
        method: UnlinkedMethod,
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
            "Loop stack size does not match original!\n\tLoop:\n\t\t${loopStack.joinToString("\n\t\t")}\n\tOriginal:\n\t\t${
                copy.joinToString("\n\t\t")
            }"
        }
        return index
    }

    override fun size(): Int {
        return block.size()
    }

    override fun toBulk(storage: List<Command>) {
        block.toBulk(storage)
    }

    override fun toBin(buffer: MemorySegmentBuffer) {
        buffer.write(2.toByte())
        block.toBin(buffer)
    }

    override fun sizeBytes(): Long {
        return 1L + block.sizeBytes()
    }
}