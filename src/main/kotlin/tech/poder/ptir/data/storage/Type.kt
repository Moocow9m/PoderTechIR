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

    data class TByte(val constant: Boolean) : Type
    data class TShort(val constant: Boolean) : Type
    data class TInt(val constant: Boolean) : Type
    data class TLong(val constant: Boolean) : Type
    data class TFloat(val constant: Boolean) : Type
    data class TDouble(val constant: Boolean) : Type
}