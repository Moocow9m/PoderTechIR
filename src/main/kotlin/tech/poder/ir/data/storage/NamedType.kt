package tech.poder.ir.data.storage

import tech.poder.ir.data.Type
import tech.poder.ir.util.MemorySegmentBuffer

data class NamedType(val name: String, val type: Type) {

    override fun toString(): String {
        return "$name = ${type::class}"
    }

    fun toString(tabCount: Int): String {
        return "${"\t".repeat(tabCount)}${toString()}"
    }

    fun size(): Int {
        return MemorySegmentBuffer.sequenceSize(name) + type.size()
    }

    fun toBin(buffer: MemorySegmentBuffer) {
        buffer.writeSequence(name)
        type.toBin(buffer)
    }

}
