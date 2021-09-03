package tech.poder.ir.data.storage

sealed interface Type {

    fun defaultValue(): Any


    data class Array(val type: Type, val size: Int) : Type {
        override fun defaultValue(): Any {
            return Array(size) { type.defaultValue() }
        }
    }

    data class Struct(val name: String, val types: List<NamedType>) : Type {
        override fun defaultValue(): Any {
            return Array(types.size) { types[it].type.defaultValue() }
        }
    }


    // Don't make these data classes unless if you want pain
    sealed class Primitive : Type {

        abstract var isConstant: Boolean


        class Byte(override var isConstant: Boolean = false) : Primitive() {
            override fun defaultValue(): Any {
                return 0.toByte()
            }
        }

        class Short(override var isConstant: Boolean = false) : Primitive() {
            override fun defaultValue(): Any {
                return 0.toShort()
            }
        }

        class Int(override var isConstant: Boolean = false) : Primitive() {
            override fun defaultValue(): Any {
                return 0
            }
        }

        class Long(override var isConstant: Boolean = false) : Primitive() {
            override fun defaultValue(): Any {
                return 0.toLong()
            }
        }

        class Float(override var isConstant: Boolean = false) : Primitive() {
            override fun defaultValue(): Any {
                return 0.toFloat()
            }
        }

        class Double(override var isConstant: Boolean = false) : Primitive() {
            override fun defaultValue(): Any {
                return 0.toDouble()
            }
        }

        class String(override var isConstant: Boolean = false) : Primitive() {
            override fun defaultValue(): Any {
                return ""
            }
        }


        override fun equals(other: Any?): Boolean {
            return other != null && this::class == other::class
        }

        override fun hashCode(): kotlin.Int {
            return this::class.hashCode()
        }

    }

}