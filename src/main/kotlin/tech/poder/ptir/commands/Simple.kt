package tech.poder.ptir.commands

import tech.poder.ptir.util.MemorySegmentBuffer
import kotlin.reflect.jvm.jvmName

sealed class Simple : Command {

    private val sizeBits by lazy { MemorySegmentBuffer.varSize(id).toLong() * 8L }

    override fun sizeBits(): Long {
        return sizeBits
    }

    override fun toString(): String {
        return this::class.simpleName ?: this::class.jvmName
    }

    override fun toBin(output: MemorySegmentBuffer) {
        output.writeVar(id)
    }

    object Return : Simple() {
        override val id: Int =
            0 //todo all IDs are subject to change based on analysis of program samples (more common get smaller numbers)
    }

    object Pop : Simple() {
        override val id: Int = 1
    }

    object Duplicate : Simple() {
        override val id: Int = 2
    }

    object Increment : Simple() {
        override val id: Int = 3
    }

    object Decrement : Simple() {
        override val id: Int = 4
    }

    object Add : Simple() {
        override val id: Int = 5
    }

    object Subtract : Simple() {
        override val id: Int = 6
    }

    object Negate : Simple() {
        override val id: Int = 7
    }

    object Multiply : Simple() {
        override val id: Int = 8
    }

    object Divide : Simple() {
        override val id: Int = 9
    }

    object Or : Simple() {
        override val id: Int = 10
    }

    object And : Simple() {
        override val id: Int = 11
    }

    object Xor : Simple() {
        override val id: Int = 12
    }

    object Sal : Simple() {
        override val id: Int = 13
    }

    object Sar : Simple() {
        override val id: Int = 14
    }

    object Shl : Simple() {
        override val id: Int = 15
    }

    object Shr : Simple() {
        override val id: Int = 16
    }

    object Rol : Simple() {
        override val id: Int = 17
    }

    object Ror : Simple() {
        override val id: Int = 18
    }

    object IfEquals : Simple() {
        override val id: Int = 19
    }

    object IfNotEquals : Simple() {
        override val id: Int = 20
    }

    object IfGreaterThan : Simple() {
        override val id: Int = 21
    }

    object IfLessThan : Simple() {
        override val id: Int = 22
    }

    object IfGreaterThanEqual : Simple() {
        override val id: Int = 23
    }

    object IfLessThanEqual : Simple() {
        override val id: Int = 24
    }

    object ArraySet : Simple() {
        override val id: Int = 25
    }

    object ArrayGet : Simple() {
        override val id: Int = 26
    }

    object ArrayCreate : Simple() {
        override val id: Int = 27
    }

    object UpscaleWithoutSign : Simple() {
        override val id: Int = 28
    }
}