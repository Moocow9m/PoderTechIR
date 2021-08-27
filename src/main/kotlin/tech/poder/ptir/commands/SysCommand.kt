package tech.poder.ptir.commands

import tech.poder.ptir.data.storage.Type

enum class SysCommand(val return_: Type?, vararg args: Type) {
    SLEEP(null, Type.Constant.TLong(false)),
    SUSPEND(null, Type.Constant.TLong(false)),
    PRINT(null, Type.Constant.TString(false)),
}