package tech.poder.ir.instructions.simple

import tech.poder.ir.instructions.Command

enum class Simple : Command {
    BREAKPOINT,
    RETURN,
    OR,
    AND,
    XOR,
    SHL,
    SHR,
    SAL,
    SAR,
    ROL,
    ROR,
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
    SET_FIELD, //todo
    GET_FIELD, //todo
    INVOKE_METHOD,
    SWITCH,
    SUSPEND, //todo
    LAUNCH, //todo
    NEW_OBJECT
}