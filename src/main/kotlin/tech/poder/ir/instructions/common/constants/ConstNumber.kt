package tech.poder.ir.instructions.common.constants

data class ConstNumber(val data: Number) : Constant, Number() {
    override fun toByte(): Byte {
        TODO("Not yet implemented")
    }

    override fun toChar(): Char {
        TODO("Not yet implemented")
    }

    override fun toDouble(): Double {
        TODO("Not yet implemented")
    }

    override fun toFloat(): Float {
        TODO("Not yet implemented")
    }

    override fun toInt(): Int {
        TODO("Not yet implemented")
    }

    override fun toLong(): Long {
        return data.toLong()
    }

    override fun toShort(): Short {
        TODO("Not yet implemented")
    }
}
