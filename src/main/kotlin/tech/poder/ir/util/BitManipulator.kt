package tech.poder.ir.util

import kotlin.experimental.and
import kotlin.experimental.xor

object BitManipulator {
    val indexes = listOf<Long>(1, 2, 4, 8, 16, 32, 64, 128, 256)

    fun Byte.isBitOn(index: Int): Boolean {
        return !isBitOff(index)
    }

    fun Byte.isBitOff(index: Int): Boolean {
        return this and indexes[index].toByte() == 0.toByte()
    }

    fun Byte.bitFlip(index: Int): Byte {
        return this xor indexes[index].toByte()
    }

    fun Byte.bitOff(index: Int): Byte {
        return if (isBitOn(index)) {
            bitFlip(index)
        } else {
            this
        }
    }

    fun Byte.bitOn(index: Int): Byte {
        return if (isBitOff(index)) {
            bitFlip(index)
        } else {
            this
        }
    }
}