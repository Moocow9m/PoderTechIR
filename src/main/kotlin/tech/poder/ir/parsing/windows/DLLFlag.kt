package tech.poder.ir.parsing.windows

import kotlin.experimental.and

enum class DLLFlag(val position: Short) {

    HIGH_ENTROPY_VA(0x0020),
    DYNAMIC_BASE(0x0040),
    FORCE_INTEGRITY(0x0080),
    NX_COMPAT(0x0100),
    NO_ISOLATION(0x0200),
    NO_SEH(0x0400),
    NO_BIND(0x0800),
    APPCONTAINER(0x1000),
    WDM_DRIVER(0x2000),
    GUARD_CF(0x4000),
    TERMINAL_SERVER_AWARE(0x8000.toShort())
    ;

    companion object {
        fun getFlags(flags: Short): List<DLLFlag> {
            val list = mutableListOf<DLLFlag>()
            values().forEach {
                if (it.position and flags != 0.toShort()) {
                    list.add(it)
                }
            }
            return list
        }
    }
}