package tech.poder.ptir.data.storage

sealed interface Type {
    fun copy(): Type

    data class TArray(val type: Type, val size: Int) : Type {
        override fun copy(): Type {
            return TArray(type, size)
        }
    }

    data class TStruct(val types: Array<NamedType>) : Type {
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
            return TStruct(types)
        }

    }

    sealed interface Constant : Type {
        var constant: Boolean

        data class TByte(override var constant: Boolean = false) : Constant {
            override fun copy(): Type {
                return TByte(constant)
            }
        }

        data class TShort(override var constant: Boolean = false) : Constant {
            override fun copy(): Type {
                return TShort(constant)
            }
        }

        data class TInt(override var constant: Boolean = false) : Constant {
            override fun copy(): Type {
                return TInt(constant)
            }
        }

        data class TLong(override var constant: Boolean = false) : Constant {
            override fun copy(): Type {
                return TLong(constant)
            }
        }

        data class TFloat(override var constant: Boolean = false) : Constant {
            override fun copy(): Type {
                return TFloat(constant)
            }
        }

        data class TDouble(override var constant: Boolean = false) : Constant {
            override fun copy(): Type {
                return TDouble(constant)
            }
        }

        data class TString(override var constant: Boolean = false) : Constant {
            override fun copy(): Type {
                return TString(constant)
            }
        }

    }
}