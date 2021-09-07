package tech.poder.ptir.data.storage

import tech.poder.ptir.data.Type

data class NamedType(val name: String, val type: Type) {

    override fun toString(): String {
        return "$name = $type"
    }

    fun toString(tabCount: Int): String {
        return "${"\t".repeat(tabCount)}${toString()}"
    }

}
