package tech.poder.ir.data.base.api

import tech.poder.ir.data.base.Method
import tech.poder.ir.data.base.Object
import tech.poder.ir.metadata.NamedIdType
import tech.poder.ir.util.MemorySegmentBuffer

data class PublicObject(val id: UInt, val name: String, val fields: List<NamedIdType>, val methods: List<Method>) :
    Object {
    override fun size(): Long {
        return 1L + MemorySegmentBuffer.varSize(id.toInt()) + MemorySegmentBuffer.varSize(fields.size) + fields.sumOf { it.size() } + MemorySegmentBuffer.varSize(
            methods.size
        ) + methods.sumOf { it.size() }
    }

    override fun save(buffer: MemorySegmentBuffer) {
        buffer.write(1.toByte())
        buffer.writeVar(id.toInt())
        buffer.writeVar(fields.size)
        fields.forEach {
            it.toBin(buffer)
        }
        buffer.writeVar(methods.size)
        methods.forEach {
            it.save(buffer)
        }
    }
}