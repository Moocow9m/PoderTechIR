package tech.poder.ir.instructions.common.special

data class Label(val id: UShort, internal var offset: Int = -2) {
    fun use(index: Int) {
        check(!isUsed()) {
            "Label already in use!"
        }
        offset = index
    }

    fun isUsed(): Boolean {
        return offset > -2
    }
}