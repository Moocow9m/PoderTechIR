package tech.poder.ir.parsing.windows

import kotlin.experimental.and

enum class CoffFlag(val position: Short) {
    RELOCS_STRIPPED(0x0001),
    EXECUTABLE_IMAGE(0x0002),
    LINE_NUMS_STRIPPED(0x0004),
    LOCAL_SYMS_STRIPPED(0x0008),
    AGGRESSIVE_WS_TRIM(0x0010),
    LARGE_ADDRESS_AWARE(0x0020),
    BYTES_REVERSED_LO(0x0080),
    NEEDS_32BIT_MACHINE(0x0100),
    DEBUG_STRIPPED(0x0200),
    REMOVABLE_RUN_FROM_SWAP(0x0400),
    NET_RUN_FROM_SWAP(0x0800),
    SYSTEM(0x1000),
    DLL(0x2000),
    UP_SYSTEM_ONLY(0x4000),
    BYTES_REVERSED_HI(0x8000.toShort());

    companion object {

        fun getFlags(flags: Short): List<CoffFlag> {
            return values().filter { it.position and flags != 0.toShort() }
        }

    }
}