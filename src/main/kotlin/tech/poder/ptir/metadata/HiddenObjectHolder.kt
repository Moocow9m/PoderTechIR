package tech.poder.ptir.metadata

import tech.poder.ptir.data.storage.NamedType

data class HiddenObjectHolder(val id: UInt, val fields: Array<NamedType>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HiddenObjectHolder) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
