package tech.poder.ptir.commands

import tech.poder.ptir.util.MemorySegmentBuffer

interface Command {
    fun id(): Int
    fun sizeBits(): Long
    fun toBin(output: MemorySegmentBuffer)
}