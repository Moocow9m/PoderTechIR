package tech.poder.ir.data

import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.util.MemorySegmentBuffer

sealed interface Type {
    fun size(): Int

    fun toBin(buffer: MemorySegmentBuffer)

    object Unit : Type { //Users cannot use this type on stack! Return type only!
        override fun size(): Int {
            return 1
        }

        override fun toBin(buffer: MemorySegmentBuffer) {
            buffer.write(0.toByte())
        }
    }

    @JvmInline
    value class RuntimeArray(val type: Type) : Type { //Array with size determined on runtime
        override fun size(): Int {
            return 1 + type.size()
        }

        override fun toBin(buffer: MemorySegmentBuffer) {
            buffer.write(10.toByte())
            type.toBin(buffer)
        }
    }

    data class Array(val type: Type, val size: Int) : Type { //Array with known size
        override fun size(): Int {
            return 1 + type.size() + MemorySegmentBuffer.varSize(size)
        }

        override fun toBin(buffer: MemorySegmentBuffer) {
            buffer.write(9.toByte())
            type.toBin(buffer)
            buffer.writeVar(size)
        }
    }

    /*@JvmInline
    value class ConstArray(val data: List<Any>) :
        Type { //Array with all data known (still technically mutable... need a method of securing immutability so copies are not created)
        override fun size(): Int {
            TODO("Not yet implemented")
        }

        override fun toBin(buffer: MemorySegmentBuffer) {
            TODO("Not yet implemented")
        }
    }*/

    data class Struct(val name: String, val types: List<NamedType>) : Type {
        override fun size(): Int {
            val typesSize = MemorySegmentBuffer.varSize(types.size)
            return 1 + MemorySegmentBuffer.sequenceSize(name) + typesSize + types.sumOf { it.size() }
        }

        override fun toBin(buffer: MemorySegmentBuffer) {
            buffer.write(8.toByte())
            buffer.writeSequence(name)
            buffer.writeVar(types.size)
            types.forEach {
                it.toBin(buffer)
            }
        }
    }


    // Don't make these data classes unless if you want pain
    sealed interface Primitive : Type {

        sealed interface Numeric : Primitive {

            sealed interface Basic : Numeric {
                object Byte : Basic {
                    override fun size(): kotlin.Int {
                        return 1
                    }

                    override fun toBin(buffer: MemorySegmentBuffer) {
                        buffer.write(1.toByte())
                    }
                }

                object Short : Basic {
                    override fun size(): kotlin.Int {
                        return 1
                    }

                    override fun toBin(buffer: MemorySegmentBuffer) {
                        buffer.write(2.toByte())
                    }
                }

                object Int : Basic {
                    override fun size(): kotlin.Int {
                        return 1
                    }

                    override fun toBin(buffer: MemorySegmentBuffer) {
                        buffer.write(3.toByte())
                    }
                }

                object Long : Basic {
                    override fun size(): kotlin.Int {
                        return 1
                    }

                    override fun toBin(buffer: MemorySegmentBuffer) {
                        buffer.write(4.toByte())
                    }
                }
            }

            sealed interface FloatingPoint : Numeric {

                object Float : FloatingPoint {
                    override fun size(): Int {
                        return 1
                    }

                    override fun toBin(buffer: MemorySegmentBuffer) {
                        buffer.write(5.toByte())
                    }
                }

                object Double : FloatingPoint {
                    override fun size(): Int {
                        return 1
                    }

                    override fun toBin(buffer: MemorySegmentBuffer) {
                        buffer.write(6.toByte())
                    }
                }
            }
        }

        object String : Primitive {
            override fun size(): Int {
                return 1
            }

            override fun toBin(buffer: MemorySegmentBuffer) {
                buffer.write(7.toByte())
            }
        }

    }

}