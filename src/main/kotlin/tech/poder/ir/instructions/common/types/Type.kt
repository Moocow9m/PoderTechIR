package tech.poder.ir.instructions.common.types

enum class Type(val sizeBits: ULong) {
    BYTE(Byte.SIZE_BITS.toULong()),
    SHORT(Short.SIZE_BITS.toULong()),
    INT(Int.SIZE_BITS.toULong()),
    LONG(Long.SIZE_BITS.toULong()),
    FLOAT(Float.SIZE_BITS.toULong()),
    DOUBLE(Double.SIZE_BITS.toULong()),
    POINTER(Long.SIZE_BITS.toULong())
}