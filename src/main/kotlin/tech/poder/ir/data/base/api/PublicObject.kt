package tech.poder.ir.data.base.api

import tech.poder.ir.data.base.Object
import tech.poder.ir.metadata.NamedType
import tech.poder.ir.util.MemorySegmentBuffer

data class PublicObject(val name: String, val fields: List<NamedType>) :
	Object {
	override fun size(): Long {
		return 1L + MemorySegmentBuffer.varSize(fields.size) + fields.sumOf { it.size() }
	}

	override fun save(buffer: MemorySegmentBuffer) {
		buffer.write(1.toByte())
		buffer.writeVar(fields.size)
		fields.forEach {
			it.toBin(buffer)
		}
	}
}