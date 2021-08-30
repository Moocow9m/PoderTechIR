package tech.poder.ir.data.storage

data class NamedType(val name: String, val type: Type) {
    override fun toString(): String {
        return "$name = $type"
    }

    fun toString(tabs: Int): String {
        val tabBuilder = StringBuilder()
        repeat(tabs) {
            tabBuilder.append('\t')
        }
        return "$tabBuilder$name = $type"
    }
}
