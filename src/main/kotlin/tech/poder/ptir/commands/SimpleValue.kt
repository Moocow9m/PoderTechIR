package tech.poder.ptir.commands

import tech.poder.ptir.util.MemorySegmentBuffer

sealed interface SimpleValue : Command {
    companion object {
        val pushId = Simple.values().size //all pushes use the same id with a byte identifier
    }

    @JvmInline
    value class PushChar(val data: Char) : Command {
        override fun id(): Int {
            return pushId
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + 1 + MemorySegmentBuffer.varSize(data.code)) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            output.write(0.toByte())
            output.writeVar(data.code)
        }
    }

    @JvmInline
    value class PushChars(val data: CharSequence) : Command {
        override fun id(): Int {
            return pushId
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + 1 + MemorySegmentBuffer.sequenceSize(data)) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            output.write(1.toByte())
            output.writeSequence(data)
        }
    }

    @JvmInline
    value class PushInt(val data: Int) : Command { // this cost an extra byte... maybe make a new class for each one?
        override fun id(): Int {
            return pushId
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + 1 + MemorySegmentBuffer.varSize(data)) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            output.write(2.toByte())
            output.writeVar(data)
        }
    }

    @JvmInline
    value class PushLong(val data: Long) : Command {
        override fun id(): Int {
            return pushId
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + 1 + MemorySegmentBuffer.varSize(data)) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            output.write(3.toByte())
            output.writeVar(data)
        }
    }

    @JvmInline
    value class PushDouble(val data: Double) : Command {
        override fun id(): Int {
            return pushId
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + 1 + MemorySegmentBuffer.varSize(data)) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            output.write(4.toByte())
            output.writeVar(data)
        }
    }

    @JvmInline
    value class PushFloat(val data: Float) : Command {
        override fun id(): Int {
            return pushId
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + 1 + MemorySegmentBuffer.varSize(data)) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            output.write(5.toByte())
            output.writeVar(data)
        }
    }

    @JvmInline
    value class PushShort(val data: Short) : Command {
        override fun id(): Int {
            return pushId
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + 1 + MemorySegmentBuffer.varSize(data)) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            output.write(6.toByte())
            output.writeVar(data)
        }
    }

    @JvmInline
    value class PushByte(val data: Byte) : Command {
        override fun id(): Int {
            return pushId
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + 2) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            output.write(7.toByte())
            output.write(data)
        }
    }

    @JvmInline
    value class PushUInt(val data: UInt) : Command { // this cost an extra byte... maybe make a new class for each one?
        override fun id(): Int {
            return pushId
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + 1 + MemorySegmentBuffer.varSize(data.toInt())) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            output.write(8.toByte())
            output.writeVar(data.toInt())
        }
    }

    @JvmInline
    value class PushULong(val data: ULong) : Command {
        override fun id(): Int {
            return pushId
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + 1 + MemorySegmentBuffer.varSize(data.toLong())) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            output.write(9.toByte())
            output.writeVar(data.toLong())
        }
    }

    @JvmInline
    value class PushUShort(val data: UShort) : Command {
        override fun id(): Int {
            return pushId
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + 1 + MemorySegmentBuffer.varSize(data.toShort())) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            output.write(10.toByte())
            output.writeVar(data.toShort())
        }
    }

    @JvmInline
    value class PushUByte(val data: UByte) : Command {
        override fun id(): Int {
            return pushId
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + 2) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            output.write(11.toByte())
            output.write(data.toByte())
        }
    }
}