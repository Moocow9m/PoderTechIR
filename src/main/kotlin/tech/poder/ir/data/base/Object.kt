package tech.poder.ir.data.base

import tech.poder.ir.util.MemorySegmentBuffer

interface Object {
	fun size(): Long

	fun save(buffer: MemorySegmentBuffer)
}