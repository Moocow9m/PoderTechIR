package tech.poder.ir.data.base.linked

import tech.poder.ir.data.base.Method
import tech.poder.ir.data.base.Object
import tech.poder.ir.data.base.Package
import tech.poder.ir.util.MemorySegmentBuffer

data class LinkedPackage(val methods: List<Method>, val objects: List<Object>) : Package {
    override fun size(): Long {
        return 1L + MemorySegmentBuffer.varSize(objects.size) + objects.sumOf { it.size() } + MemorySegmentBuffer.varSize(
            methods.size
        ) + methods.sumOf { it.size() }
    }

    override fun save(buffer: MemorySegmentBuffer) {
        buffer.write(1.toByte())
        buffer.writeVar(objects.size)
        objects.forEach {
            it.save(buffer)
        }
        buffer.writeVar(methods.size)
        methods.forEach {
            it.save(buffer)
        }
    }
}
