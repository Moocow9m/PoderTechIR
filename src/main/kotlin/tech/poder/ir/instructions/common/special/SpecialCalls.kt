package tech.poder.ir.instructions.common.special

enum class SpecialCalls(val remove: Int = 0, val add: Int = 0, val argRange: IntRange = IntRange(0, 0)) {
    PRINT(1),
    SYSTEM_IN(add = 1),
    RANDOM_INT(add = 1, argRange = 0..2),
}