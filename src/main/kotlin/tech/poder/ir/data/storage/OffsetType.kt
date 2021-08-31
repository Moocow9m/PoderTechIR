package tech.poder.ir.data.storage

data class OffsetType(val offset: Int, val type: Type) {
    override fun toString(): String {
        return "$offset = $type"
    }

    fun toString(tabs: Int): String {
        val tabBuilder = StringBuilder()
        repeat(tabs) {
            tabBuilder.append('\t')
        }
        return "$tabBuilder$offset = $type"
    }
}
