package tech.poder.ir.data.storage

data class Label internal constructor(val id: UShort, internal var offset: Int = -2) {

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
