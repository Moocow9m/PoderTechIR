package tech.poder.ir.commands

import tech.poder.ir.util.MemorySegmentBuffer

interface Command {
	fun id(): Int
	fun sizeBits(): Long
	fun toBin(output: MemorySegmentBuffer)
}