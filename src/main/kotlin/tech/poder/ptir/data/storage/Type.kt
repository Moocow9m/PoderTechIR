package tech.poder.ptir.data.storage

sealed interface Type {
    data class TArray(val types: Array<Type>, val size: UInt) : Type {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is TArray) return false

            if (!types.contentEquals(other.types)) return false
            if (size != other.size) return false

            return true
        }

        override fun hashCode(): Int {
            var result = types.contentHashCode()
            result = 31 * result + size.hashCode()
            return result
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

    }

    sealed interface Constant : Type {
        var constant: Boolean

        data class TByte(override var constant: Boolean) : Constant
        data class TShort(override var constant: Boolean) : Constant
        data class TInt(override var constant: Boolean) : Constant
        data class TLong(override var constant: Boolean) : Constant
        data class TFloat(override var constant: Boolean) : Constant
        data class TDouble(override var constant: Boolean) : Constant
        data class TString(override var constant: Boolean) : Constant
    }
}