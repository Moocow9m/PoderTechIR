package tech.poder.ir.metadata

import tech.poder.ir.data.Type
import tech.poder.ir.util.MemorySegmentBuffer

data class NamedIdType(val name: String, val id: UInt, val type: Type) {

	override fun toString(): String {
		return "$name = ${type::class}"
	}

	fun toString(tabCount: Int): String {
		return "${"\t".repeat(tabCount)}${toString()}"
	}

	fun size(): Int {
		return MemorySegmentBuffer.sequenceSize(name) + MemorySegmentBuffer.varSize(id.toInt()) + type.size()
	}

	fun toBin(buffer: MemorySegmentBuffer) {
		buffer.writeSequence(name)
		buffer.writeVar(id.toInt())
		type.toBin(buffer)
	}

}
