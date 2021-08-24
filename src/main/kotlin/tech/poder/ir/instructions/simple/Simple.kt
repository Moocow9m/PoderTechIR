package tech.poder.ir.instructions.simple

import tech.poder.ir.instructions.Command

enum class Simple : Command {
    BREAKPOINT,
    MARKER,
    RETURN,
    INC,
    DEC,
    ADD,
    SUB,
    MUL,
    DIV,
    NEG,
    IF_EQ,
    IF_GT,
    IF_LT,
    IF_GT_EQ,
    IF_LT_EQ,
    IF_NEQ,
    PUSH,
    POP,
    DUP,
    JMP,
    ARRAY_SET,
    ARRAY_GET,
    ARRAY_CREATE,
    STORE_VAR,
    GET_VAR,
    SET_FIELD,
    GET_FIELD,
    INVOKE_METHOD,
    SWITCH,
    SUSPEND,
    LAUNCH,
    NEW_OBJECT
}