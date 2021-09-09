package tech.poder.ir.commands

import tech.poder.ir.util.MemorySegmentBuffer

interface DebugValue : Command {
    companion object {
        val debugId = SimpleValue.last + 1
    }

    @JvmInline
    value class LineNumber(val line: UInt) : DebugValue {
        override fun id(): Int {
            return debugId
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(debugId) + MemorySegmentBuffer.varSize(line.toInt())) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(debugId)
            output.writeVar(line.toInt())
        }
    }
}