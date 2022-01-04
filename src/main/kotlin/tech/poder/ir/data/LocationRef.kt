package tech.poder.ir.data

import tech.poder.ir.util.MemorySegmentBuffer

sealed interface LocationRef {
	fun toBin(buffer: MemorySegmentBuffer)

	fun size(): Int

	@JvmInline
	value class LocationByName(val name: CharSequence) : LocationRef {
		override fun toBin(buffer: MemorySegmentBuffer) {
			buffer.write(1.toByte())
			buffer.writeSequence(name)
		}

		override fun size(): Int {
			return 1 + MemorySegmentBuffer.sequenceSize(name)
		}
	}

	@JvmInline
	value class LocationByID(val id: UInt) : LocationRef {
		override fun toBin(buffer: MemorySegmentBuffer) {
			buffer.write(0.toByte())
			buffer.writeVar(id.toInt())
		}

		override fun size(): Int {
			return 1 + MemorySegmentBuffer.varSize(id.toInt())
		}
	}

	@JvmInline
	value class LocationByCId(val id: Pair<UInt, UInt>) : LocationRef {
		override fun toBin(buffer: MemorySegmentBuffer) {
			buffer.write(2.toByte())
			buffer.writeVar(id.first.toInt())
			buffer.writeVar(id.second.toInt())
		}

		override fun size(): Int {
			return 1 + MemorySegmentBuffer.varSize(id.first.toInt()) + MemorySegmentBuffer.varSize(id.second.toInt())
		}
	}
}
