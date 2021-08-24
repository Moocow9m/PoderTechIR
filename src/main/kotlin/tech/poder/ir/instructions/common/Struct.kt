package tech.poder.ir.instructions.common

import tech.poder.ir.instructions.common.types.Type

data class Struct(val size: ULong, val types: Array<out Type>, var names: Array<String>) {
    companion object {
        fun build(types: Array<out Type>, names: Array<String>): Struct? {
            if (types.isEmpty()) {
                return null
            }
            var size = 0uL
            types.forEach {
                size += it.sizeBits
            }

            return Struct(size, types, names)
        }
    }
}