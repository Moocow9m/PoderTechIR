package tech.poder.ptir.metadata

import tech.poder.ptir.data.storage.NamedType

data class ObjectHolder(val fullName: String, val fields: Array<NamedType>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjectHolder) return false

        if (fullName != other.fullName) return false

        return true
    }

    override fun hashCode(): Int {
        return fullName.hashCode()
    }
}
