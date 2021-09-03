package tech.poder.ir.util

import jdk.incubator.foreign.MemoryAccess
import jdk.incubator.foreign.MemorySegment
import java.nio.ByteOrder

data class MemorySegmentReader(
    val memorySegment: MemorySegment,
    val byteOrder: ByteOrder = ByteOrder.nativeOrder(),
) : AutoCloseable {

    var position = 0L


    override fun close() {
        memorySegment.close()
    }


    fun readByte(): Byte {
        return MemoryAccess.getByteAtOffset(memorySegment, position).apply {
            position += Byte.SIZE_BYTES
        }
    }

    fun readShort(): Short {
        return MemoryAccess.getShortAtOffset(memorySegment, position, byteOrder).apply {
            position += Short.SIZE_BYTES
        }
    }

    fun readInt(): Int {
        return MemoryAccess.getIntAtOffset(memorySegment, position, byteOrder).apply {
            position += Int.SIZE_BYTES
        }
    }

    fun readLong(): Long {
        return MemoryAccess.getLongAtOffset(memorySegment, position, byteOrder).apply {
            position += Long.SIZE_BYTES
        }
    }


    fun readUByte(): UByte {
        return readByte().toUByte()
    }

    fun readUShort(): UShort {
        return readShort().toUShort()
    }

    fun readUInt(): UInt {
        return readInt().toUInt()
    }

    fun readULong(): ULong {
        return readLong().toULong()
    }


    fun readChar(): Char {
        return MemoryAccess.getCharAtOffset(memorySegment, position, byteOrder).apply {
            position += Char.SIZE_BYTES
        }
    }

    fun readAsciiChar(): Char {
        return readByte().toInt().toChar()
    }


    fun skip(bytes: Int) {
        position += bytes
    }

}
