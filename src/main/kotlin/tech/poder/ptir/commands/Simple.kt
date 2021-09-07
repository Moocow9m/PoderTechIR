package tech.poder.ptir.commands

import tech.poder.ptir.util.MemorySegmentBuffer

enum class Simple : Command {
    //todo all IDs are subject to change based on analysis of program samples (more common get smaller numbers)
    RETURN,
    POP,
    DUP,
    INC,
    DEC,
    ADD,
    SUB,
    NEG,
    MUL,
    DIV,
    OR,
    AND,
    XOR,
    SAL,
    SAR,
    SHL,
    SHR,
    ROL,
    ROR,
    ARRAY_SET,
    ARRAY_GET,
    ARRAY_CREATE,
    UNSAFE_GET,
    UNSAFE_SET;

    val id: Int = ordinal
    private val sizeBits = MemorySegmentBuffer.varSize(id).toLong() * 8L

    override fun id(): Int {
        return id
    }

    override fun sizeBits(): Long {
        return sizeBits
    }

    override fun toString(): String {
        return "[$id] $name"
    }

    override fun toBin(output: MemorySegmentBuffer) {
        output.writeVar(id)
    }
}