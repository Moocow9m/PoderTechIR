package tech.poder.ir.data.base.linked

import tech.poder.ir.commands.Command
import tech.poder.ir.data.base.Method
import tech.poder.ir.util.MemorySegmentBuffer
import kotlin.math.ceil

data class PrivateMethod(val id: UInt, val argsSize: Byte, val returns: Boolean, val instructions: List<Command>) :
    Method {
    override fun size(): Long {
        return 3L + MemorySegmentBuffer.varSize(instructions.size) + ceil(
            instructions.sumOf { it.sizeBits() } / 8.0).toLong()
    }

    override fun save(buffer: MemorySegmentBuffer) {
        buffer.write(2.toByte())
        buffer.write(argsSize)
        if (returns) {
            buffer.write(1.toByte())
        } else {
            buffer.write(0.toByte())
        }
        buffer.writeVar(instructions.size)
        instructions.forEach {
            it.toBin(buffer)
        }
    }
}