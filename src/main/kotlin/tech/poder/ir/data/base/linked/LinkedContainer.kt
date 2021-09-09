package tech.poder.ir.data.base.linked

import tech.poder.ir.data.base.Container
import tech.poder.ir.util.MemorySegmentBuffer

data class LinkedContainer(
    override val name: String,
    val entryPoint: UInt = 0u,
    val packages: List<LinkedPackage>
) : Container {
    override fun size(): Long {
        return 1 + MemorySegmentBuffer.sequenceSize(name) + MemorySegmentBuffer.varSize(entryPoint.toInt()) + MemorySegmentBuffer.varSize(
            packages.size
        ) + packages.sumOf { it.size() }
    }

    override fun save(buffer: MemorySegmentBuffer) {
        buffer.write(1.toByte())
        buffer.writeSequence(name)
        buffer.writeVar(entryPoint.toInt())
        buffer.writeVar(packages.size)
        packages.forEach {
            it.save(buffer)
        }
    }
}
