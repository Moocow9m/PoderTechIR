package tech.poder.ir.commands

import tech.poder.ir.data.Label
import tech.poder.ir.data.LocationRef
import tech.poder.ir.data.Type
import tech.poder.ir.util.MemorySegmentBuffer

sealed interface SimpleValue : Command {
    companion object {
        private val pushId = Simple.values().size //all pushes use the same id with a byte identifier
        private val getVar = pushId + 1
        private val setVar = getVar + 1
        private val jump = setVar + 1
        private val ifEquals = jump + 1 //should ifs be combined?
        private val ifNEquals = ifEquals + 1
        private val ifGT = ifNEquals + 1
        private val ifLT = ifGT + 1
        private val ifLTEquals = ifLT + 1
        private val ifGTEquals = ifLTEquals + 1
        private val sysCall = ifGTEquals + 1
        private val setField = sysCall + 1
        private val getField = setField + 1
        private val invokeMethod = getField + 1
        private val launch = invokeMethod + 1
        private val newObject = launch + 1
        private val arrayCreate = newObject + 1
        private val unsafeGet = arrayCreate + 1
        private val unsafeSet = unsafeGet + 1
        internal val last = unsafeSet
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

    @JvmInline
    value class SetVar(val data: LocationRef) : Command {
        override fun id(): Int {
            return setVar
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + data.size()) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            data.toBin(output)
        }
    }

    @JvmInline
    value class GetVar(val data: LocationRef) : Command {
        override fun id(): Int {
            return getVar
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + data.size()) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            data.toBin(output)
        }
    }

    @JvmInline
    value class Jump(val data: Label) : Command {
        override fun id(): Int {
            return jump
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + MemorySegmentBuffer.varSize(data.id.toShort()) + MemorySegmentBuffer.varSize(
                data.offset
            )) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            output.writeVar(data.id.toShort())
            output.writeVar(data.offset)
        }
    }

    sealed interface IfType : Command {
        fun label(): Label

        @JvmInline
        value class IfEquals(val data: Label) : IfType {
            override fun label(): Label {
                return data
            }

            override fun id(): Int {
                return ifEquals
            }

            override fun sizeBits(): Long {
                return (MemorySegmentBuffer.varSize(id()) + MemorySegmentBuffer.varSize(data.id.toShort()) + MemorySegmentBuffer.varSize(
                    data.offset
                )) * 8L
            }

            override fun toBin(output: MemorySegmentBuffer) {
                output.writeVar(id())
                output.writeVar(data.id.toShort())
                output.writeVar(data.offset)
            }
        }

        @JvmInline
        value class IfNotEquals(val data: Label) : IfType {
            override fun label(): Label {
                return data
            }

            override fun id(): Int {
                return ifNEquals
            }

            override fun sizeBits(): Long {
                return (MemorySegmentBuffer.varSize(id()) + MemorySegmentBuffer.varSize(data.id.toShort()) + MemorySegmentBuffer.varSize(
                    data.offset
                )) * 8L
            }

            override fun toBin(output: MemorySegmentBuffer) {
                output.writeVar(id())
                output.writeVar(data.id.toShort())
                output.writeVar(data.offset)
            }
        }

        @JvmInline
        value class IfGreaterThan(val data: Label) : IfType {
            override fun label(): Label {
                return data
            }

            override fun id(): Int {
                return ifGT
            }

            override fun sizeBits(): Long {
                return (MemorySegmentBuffer.varSize(id()) + MemorySegmentBuffer.varSize(data.id.toShort()) + MemorySegmentBuffer.varSize(
                    data.offset
                )) * 8L
            }

            override fun toBin(output: MemorySegmentBuffer) {
                output.writeVar(id())
                output.writeVar(data.id.toShort())
                output.writeVar(data.offset)
            }
        }

        @JvmInline
        value class IfLessThan(val data: Label) : IfType {
            override fun label(): Label {
                return data
            }

            override fun id(): Int {
                return ifLT
            }

            override fun sizeBits(): Long {
                return (MemorySegmentBuffer.varSize(id()) + MemorySegmentBuffer.varSize(data.id.toShort()) + MemorySegmentBuffer.varSize(
                    data.offset
                )) * 8L
            }

            override fun toBin(output: MemorySegmentBuffer) {
                output.writeVar(id())
                output.writeVar(data.id.toShort())
                output.writeVar(data.offset)
            }
        }

        @JvmInline
        value class IfLessThanEquals(val data: Label) : IfType {
            override fun label(): Label {
                return data
            }

            override fun id(): Int {
                return ifLTEquals
            }

            override fun sizeBits(): Long {
                return (MemorySegmentBuffer.varSize(id()) + MemorySegmentBuffer.varSize(data.id.toShort()) + MemorySegmentBuffer.varSize(
                    data.offset
                )) * 8L
            }

            override fun toBin(output: MemorySegmentBuffer) {
                output.writeVar(id())
                output.writeVar(data.id.toShort())
                output.writeVar(data.offset)
            }
        }

        @JvmInline
        value class IfGreaterThanEquals(val data: Label) : IfType {
            override fun label(): Label {
                return data
            }

            override fun id(): Int {
                return ifGTEquals
            }

            override fun sizeBits(): Long {
                return (MemorySegmentBuffer.varSize(id()) + MemorySegmentBuffer.varSize(data.id.toShort()) + MemorySegmentBuffer.varSize(
                    data.offset
                )) * 8L
            }

            override fun toBin(output: MemorySegmentBuffer) {
                output.writeVar(id())
                output.writeVar(data.id.toShort())
                output.writeVar(data.offset)
            }
        }

    }

    @JvmInline
    value class SystemCall(val data: SysCommand) : Command {
        override fun id(): Int {
            return sysCall
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + MemorySegmentBuffer.varSize(data.ordinal)) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            output.writeVar(data.ordinal)
        }
    }

    @JvmInline
    value class GetField(val data: LocationRef) : Command {
        override fun id(): Int {
            return getField
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + data.size()) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            data.toBin(output)
        }
    }

    @JvmInline
    value class SetField(val data: LocationRef) : Command {
        override fun id(): Int {
            return setField
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + data.size()) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            data.toBin(output)
        }
    }

    @JvmInline
    value class Invoke(val data: LocationRef) : Command {
        override fun id(): Int {
            return invokeMethod
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + data.size()) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            data.toBin(output)
        }
    }

    @JvmInline
    value class Launch(val data: LocationRef) : Command {
        override fun id(): Int {
            return launch
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + data.size()) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            data.toBin(output)
        }
    }

    @JvmInline
    value class NewObject(val data: LocationRef) : Command {
        override fun id(): Int {
            return newObject
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + data.size()) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            data.toBin(output)
        }
    }

    @JvmInline
    value class ArrayCreate(val data: Type) : Command {
        override fun id(): Int {
            return arrayCreate
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + data.size()) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            data.toBin(output)
        }
    }

    @JvmInline
    value class UnsafeGet(val data: Type) : Command {
        override fun id(): Int {
            return unsafeGet
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + data.size()) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            data.toBin(output)
        }
    }

    @JvmInline
    value class UnsafeSet(val data: Type) : Command {
        override fun id(): Int {
            return unsafeSet
        }

        override fun sizeBits(): Long {
            return (MemorySegmentBuffer.varSize(id()) + data.size()) * 8L
        }

        override fun toBin(output: MemorySegmentBuffer) {
            output.writeVar(id())
            data.toBin(output)
        }
    }
}