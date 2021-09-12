package tech.poder.ir.data.base.api

import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Method
import tech.poder.ir.util.MemorySegmentBuffer

data class PublicMethod(
    val name: String,
    val args: List<Type>,
    val returns: Type
) : Method {
    override fun size(): Long {
        return 1L + MemorySegmentBuffer.sequenceSize(name) + MemorySegmentBuffer.varSize(args.size) + args.sumOf {
            it.size().toLong()
        } + returns.size()
    }

    override fun save(buffer: MemorySegmentBuffer) {
        buffer.write(1.toByte())
        buffer.writeSequence(name)
        buffer.writeVar(args.size)
        args.forEach {
            it.toBin(buffer)
        }
        returns.toBin(buffer)
    }
}
