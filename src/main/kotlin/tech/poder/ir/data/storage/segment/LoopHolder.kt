package tech.poder.ir.data.storage.segment

import tech.poder.ir.commands.Command
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Container
import tech.poder.ir.data.base.unlinked.UnlinkedMethod
import tech.poder.ir.util.MemorySegmentBuffer
import java.util.*

@JvmInline
value class LoopHolder(val block: Segment) : Segment {

    override fun eval(
        dependencies: Set<Container>,
        self: Container,
        method: UnlinkedMethod,
        stack: Stack<Type>,
        currentIndex: Int
    ): Int {

        val prevSize = stack.size

        val index = block.eval(dependencies, self, method, stack, currentIndex)

        check(stack.size == prevSize) {
            "Loop stack size does not match original! (leaky stack?) Original: $prevSize New: ${stack.size}"
        }

        return index
    }

    override fun size(): Int {
        return block.size()
    }

    override fun toBulk(storage: MutableList<Command>) {
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