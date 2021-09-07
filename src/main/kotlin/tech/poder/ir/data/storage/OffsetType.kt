package tech.poder.ir.data.storage

import tech.poder.ptir.data.Type

data class OffsetType(val offset: Int, val type: Type) {

    override fun toString(): String {
        return "$offset = $type"
    }

    fun toString(tabCount: Int): String {
        return "${"\t".repeat(tabCount)}${toString()}"
    }

}
