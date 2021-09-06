package tech.poder.ptir.commands

import tech.poder.ptir.util.MemorySegmentBuffer

internal sealed interface Simple : Command {
    object Continue : Simple {
        override val id: Int = 0

        private val sizeBits = MemorySegmentBuffer.varSize(id).toLong() * 8L

        override fun sizeBits(): Long {
            return sizeBits
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id)
        }

        override fun toString(): String {
            return "continue"
        }
    }
}