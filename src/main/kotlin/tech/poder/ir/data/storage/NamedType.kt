package tech.poder.ir.data.storage

data class NamedType(val name: String, val type: Type) {

    override fun toString(): String {
        return "$name = $type"
    }

    fun toString(tabCount: Int): String {
        return "${"\t".repeat(tabCount)}${toString()}"
    }

}
