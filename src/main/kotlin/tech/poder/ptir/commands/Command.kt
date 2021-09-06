package tech.poder.ptir.commands

import tech.poder.ptir.util.MemorySegmentBuffer

internal interface Command {
    val id: Int
    fun sizeBits(): Long
    fun toBin(output: MemorySegmentBuffer)
}