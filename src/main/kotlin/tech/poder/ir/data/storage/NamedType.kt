package tech.poder.ir.data.storage

import tech.poder.ir.data.Type

data class NamedType(val name: String, val type: Type) {

    override fun toString(): String {
        return "$name = ${type::class}"
    }

    fun toString(tabCount: Int): String {
        return "${"\t".repeat(tabCount)}${toString()}"
    }

}
