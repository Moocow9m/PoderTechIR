package tech.poder.ir.data.storage

import tech.poder.ptir.data.Type
import kotlin.reflect.KClass

data class NamedType(val name: String, val type: KClass<out Type>) {

    override fun toString(): String {
        return "$name = $type"
    }

    fun toString(tabCount: Int): String {
        return "${"\t".repeat(tabCount)}${toString()}"
    }

}
