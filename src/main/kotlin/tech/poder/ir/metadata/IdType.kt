package tech.poder.ir.metadata

import tech.poder.ir.data.Type
import tech.poder.ir.util.MemorySegmentBuffer

data class IdType(val id: UInt, val type: Type) {

    override fun toString(): String {
        return "$id = ${type::class}"
    }

    fun toString(tabCount: Int): String {
        return "${"\t".repeat(tabCount)}${toString()}"
    }

    fun size(): Int {
        return MemorySegmentBuffer.varSize(id.toInt()) + type.size()
    }

    fun toBin(buffer: MemorySegmentBuffer) {
        buffer.writeVar(id.toInt())
        type.toBin(buffer)
    }
}

