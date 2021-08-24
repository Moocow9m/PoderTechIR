package tech.poder.ir.instructions.complex

import tech.poder.ir.instructions.Command

enum class Complex : Command {
    BYTE_TO_FLOAT,
    BYTE_TO_DOUBLE,
    INT_TO_FLOAT,
    INT_TO_DOUBLE,
    LONG_TO_FLOAT,
    LONG_TO_DOUBLE,
    DOUBLE_TO_LONG,
    FLOAT_TO_INT,
    DECREMENT_REFERENCE,
    INCREMENT_REFERENCE,
    IF_ZERO,
    COMPARE,
    SYS_CALL
}