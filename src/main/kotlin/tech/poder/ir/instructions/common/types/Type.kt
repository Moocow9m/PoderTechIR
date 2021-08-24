package tech.poder.ir.instructions.common.types

enum class Type(val sizeBits: ULong, val default: Any?) {
    BYTE(Byte.SIZE_BITS.toULong(), 0.toByte()),
    SHORT(Short.SIZE_BITS.toULong(), 0.toShort()),
    INT(Int.SIZE_BITS.toULong(), 0),
    LONG(Long.SIZE_BITS.toULong(), 0.toLong()),
    FLOAT(Float.SIZE_BITS.toULong(), 0.toFloat()),
    DOUBLE(Double.SIZE_BITS.toULong(), 0.toDouble()),
    POINTER(Long.SIZE_BITS.toULong(), null)
}