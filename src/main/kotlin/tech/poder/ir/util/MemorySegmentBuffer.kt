package tech.poder.ir.util

import jdk.incubator.foreign.MemoryAccess
import jdk.incubator.foreign.MemorySegment
import java.nio.ByteOrder

data class MemorySegmentBuffer internal constructor(
    val memorySegment: MemorySegment,
    val byteOrder: ByteOrder = ByteOrder.nativeOrder(),
) : AutoCloseable {

    companion object {
        /*fun varSize(number: Number): Byte {
            return (when (number) {
                is Int -> varSize(number)
                is Long -> varSize(number)
                is Double -> varSize(number)
                is Float -> varSize(number)
                is Short -> varSize(number)
                else -> error("Variable length number not supported on: ${number::class}")
            } + 1).toByte()
        }*/

        fun varSize(short: Short): Byte {
            return varSize(short.toInt())
        }

        fun varSize(int: Int): Byte {
            var remainder = int
            var result: Byte = 0
            do {
                result++
                remainder = remainder ushr 7
            } while (remainder != 0)
            return result
        }

        fun varSize(float: Float): Byte {
            return varSize(float.toBits())
        }

        fun varSize(long: Long): Byte {
            var remainder = long
            var result: Byte = 0
            do {
                result++
                remainder = remainder ushr 7
            } while (remainder != 0L)
            return result
        }

        fun varSize(double: Double): Byte {
            return varSize(double.toBits())
        }

        fun sequenceSize(charSequence: CharSequence): Int {
            var bytes = 1
            charSequence.forEach {
                bytes += varSize(it.code)
            }
            return bytes
        }
    }

    var position = 0L


    override fun close() {
        memorySegment.close()
    }

    fun writeVar(short: Short) {
        writeVar(short.toInt())
    }

    fun writeVar(int: Int) {
        var remainder = int
        do {
            val bits = remainder and 0x7f
            remainder = remainder ushr 7
            if (remainder == 0) {
                write(bits.toByte())
            } else {
                write((bits or 0x80).toByte())
            }
        } while (remainder != 0)
    }

    fun writeVar(float: Float) {
        writeVar(float.toBits())
    }

    fun writeVar(long: Long) {
        var remainder = long
        do {
            val bits = remainder and 0x7f
            remainder = remainder ushr 7
            if (remainder == 0L) {
                write(bits.toByte())
            } else {
                write((bits or 0x80).toByte())
            }
        } while (remainder != 0L)
    }

    fun writeVar(double: Double) {
        writeVar(double.toBits())
    }

    fun write(byte: Byte) {
        return MemoryAccess.setByteAtOffset(memorySegment, position, byte).apply {
            position += Byte.SIZE_BYTES
        }
    }

    fun write(long: Long) {
        return MemoryAccess.setLongAtOffset(memorySegment, position, long).apply {
            position += Long.SIZE_BYTES
        }
    }

    /*fun writeVar(number: Number) {
        when (number) {
            is Int -> {
                write(0.toByte())
                writeVar(number)
            }
            is Long -> {
                write(1.toByte())
                writeVar(number)
            }
            is Double -> {
                write(2.toByte())
                writeVar(number)
            }
            is Float -> {
                write(3.toByte())
                writeVar(number)
            }
            is Short -> {
                write(4.toByte())
                writeVar(number)
            }
            else -> error("Variable length number not supported on: ${number::class}")
        }
    }*/

    fun writeSequence(charSequence: CharSequence) {
        charSequence.forEach {
            writeVar(it.code)
        }
        write(0.toByte()) //null terminator
    }

    fun readByte(): Byte {
        return MemoryAccess.getByteAtOffset(memorySegment, position).apply {
            position += Byte.SIZE_BYTES
        }
    }

    fun peakByte(): Byte {
        return MemoryAccess.getByteAtOffset(memorySegment, position)
    }

    /*fun readVarNumber(): Number {
        return when (readByte()) {
            0.toByte() -> {
                readVarInt()
            }
            1.toByte() -> {
                readVarLong()
            }
            2.toByte() -> {
                readVarDouble()
            }
            3.toByte() -> {
                readVarFloat()
            }
            4.toByte() -> {
                readShort()
            }
            else -> error("Variable length decoding error!")
        }
    }*/

    fun readVarShort(): Short {
        return readVarInt().toShort()
    }

    fun readShort(): Short {
        return MemoryAccess.getShortAtOffset(memorySegment, position, byteOrder).apply {
            position += Short.SIZE_BYTES
        }
    }

    fun readVarInt(): Int {
        var result = 0
        var shiftSize = 0
        var remainder: Int
        do {
            check(shiftSize < 32) {
                "VarInt too big!"
            }
            remainder = readByte().toInt()
            result = result or (remainder and 0x7F shl shiftSize)
            shiftSize += 7
        } while (remainder and 0x80 != 0)
        return result
    }

    fun readInt(): Int {
        return MemoryAccess.getIntAtOffset(memorySegment, position, byteOrder).apply {
            position += Int.SIZE_BYTES
        }
    }

    fun readVarFloat(): Float {
        return Float.fromBits(readVarInt())
    }

    fun readVarLong(): Long {
        var result = 0L
        var shiftSize = 0
        var remainder: Long
        do {
            check(shiftSize < 32) {
                "VarInt too big!"
            }
            remainder = readByte().toLong()
            result = result or (remainder and 0x7F shl shiftSize)
            shiftSize += 7
        } while (remainder and 0x80 != 0L)
        return result
    }

    fun readVarDouble(): Double {
        return Double.fromBits(readVarLong())
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

    fun readCString(): String {
        return buildString {
            var i = readAsciiChar()

            while (i != '\u0000') {
                append(i)
                i = readAsciiChar()
            }
        }
    }

    fun readSequence(): CharSequence {
        return buildString {
            while (peakByte() != 0.toByte()) {
                append(Char(readVarInt()))
            }
            skip(1)
        }
    }

    fun readAsciiString(size: Int): String {
        return buildString {
            repeat(size) {
                append(readAsciiChar())
            }
        }
    }

    fun skip(bytes: Int) {
        position += bytes
    }

    fun remaining(): Long {
        return memorySegment.byteSize() - position
    }

}
