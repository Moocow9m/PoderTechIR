package tech.poder.ir.data.storage

sealed interface Type {

    fun defaultValue(): Any


    data class TArray(val type: Type, val size: Int) : Type {
        override fun defaultValue(): Any {
            return Array(size) { type.defaultValue() }
        }
    }

    data class TStruct(val name: String, val types: List<NamedType>) : Type {
        override fun defaultValue(): Any {
            return Array(types.size) { types[it].type.defaultValue() }
        }
    }


    // Don't make these data classes unless if you want pain
    sealed class Primitive : Type {

        abstract var isConstant: Boolean


        class TByte(override var isConstant: Boolean = false) : Primitive() {
            override fun defaultValue(): Any {
                return 0.toByte()
            }
        }

        class TShort(override var isConstant: Boolean = false) : Primitive() {
            override fun defaultValue(): Any {
                return 0.toShort()
            }
        }

        class TInt(override var isConstant: Boolean = false) : Primitive() {
            override fun defaultValue(): Any {
                return 0
            }
        }

        class TLong(override var isConstant: Boolean = false) : Primitive() {
            override fun defaultValue(): Any {
                return 0.toLong()
            }
        }

        class TFloat(override var isConstant: Boolean = false) : Primitive() {
            override fun defaultValue(): Any {
                return 0.toFloat()
            }
        }

        class TDouble(override var isConstant: Boolean = false) : Primitive() {
            override fun defaultValue(): Any {
                return 0.toDouble()
            }
        }

        class TString(override var isConstant: Boolean = false) : Primitive() {
            override fun defaultValue(): Any {
                return ""
            }
        }


        override fun equals(other: Any?): Boolean {
            return other != null && this::class == other::class
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }

    }

}