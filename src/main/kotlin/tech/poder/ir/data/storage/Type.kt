package tech.poder.ir.data.storage

sealed interface Type {
    fun copy(): Type

    fun defaultValue(): Any

    data class TArray(val type: Type, val size: Int) : Type {
        override fun copy(): Type {
            return TArray(type, size)
        }

        override fun defaultValue(): Any {
            return Array(size) { type.defaultValue() }
        }
    }

    data class TStruct(val name: String, val types: Array<NamedType>) : Type {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is TStruct) return false

            if (!types.contentEquals(other.types)) return false

            return true
        }

        override fun hashCode(): Int {
            return types.contentHashCode()
        }

        override fun copy(): Type {
            return TStruct(name, types)
        }

        override fun defaultValue(): Any {
            return Array(types.size) { types[it].type.defaultValue() }
        }

    }

    sealed interface Constant : Type {
        var constant: Boolean

        data class TByte(override var constant: Boolean = false) : Constant {
            override fun copy(): Type {
                return TByte(constant)
            }

            override fun defaultValue(): Any {
                return 0.toByte()
            }
        }

        data class TShort(override var constant: Boolean = false) : Constant {
            override fun copy(): Type {
                return TShort(constant)
            }

            override fun defaultValue(): Any {
                return 0.toShort()
            }
        }

        data class TInt(override var constant: Boolean = false) : Constant {
            override fun copy(): Type {
                return TInt(constant)
            }

            override fun defaultValue(): Any {
                return 0
            }
        }

        data class TLong(override var constant: Boolean = false) : Constant {
            override fun copy(): Type {
                return TLong(constant)
            }

            override fun defaultValue(): Any {
                return 0.toLong()
            }
        }

        data class TFloat(override var constant: Boolean = false) : Constant {
            override fun copy(): Type {
                return TFloat(constant)
            }

            override fun defaultValue(): Any {
                return 0.toFloat()
            }
        }

        data class TDouble(override var constant: Boolean = false) : Constant {
            override fun copy(): Type {
                return TDouble(constant)
            }

            override fun defaultValue(): Any {
                return 0.toDouble()
            }
        }

        data class TString(override var constant: Boolean = false) : Constant {
            override fun copy(): Type {
                return TString(constant)
            }

            override fun defaultValue(): Any {
                return ""
            }
        }

    }
}