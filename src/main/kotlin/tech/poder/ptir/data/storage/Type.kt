package tech.poder.ptir.data.storage

sealed interface Type {
    data class TArray(val type: Type, val size: Int) : Type

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

        data class TByte(override var constant: Boolean = false) : Constant
        data class TShort(override var constant: Boolean = false) : Constant
        data class TInt(override var constant: Boolean = false) : Constant
        data class TLong(override var constant: Boolean = false) : Constant
        data class TFloat(override var constant: Boolean = false) : Constant
        data class TDouble(override var constant: Boolean = false) : Constant
        data class TString(override var constant: Boolean = false) : Constant
    }
}