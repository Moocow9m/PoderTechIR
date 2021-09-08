package tech.poder.ir.data.base.linked

import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Object
import tech.poder.ir.util.MemorySegmentBuffer

data class PrivateObject(val id: UInt, val fields: List<Type>, val methods: List<PrivateMethod>) : Object {
    override fun size(): Long {
        return 1L + MemorySegmentBuffer.varSize(id.toInt()) + MemorySegmentBuffer.varSize(fields.size) + fields.sumOf { it.size() } + MemorySegmentBuffer.varSize(
            methods.size
        ) + methods.sumOf { it.size() }
    }

    override fun save(buffer: MemorySegmentBuffer) {
        buffer.write(2.toByte())
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