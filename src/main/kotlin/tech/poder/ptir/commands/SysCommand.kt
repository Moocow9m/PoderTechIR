package tech.poder.ptir.commands

import tech.poder.ptir.data.storage.Type

enum class SysCommand(val return_: Type?, vararg val args: Type) {
    SLEEP(null, Type.Constant.TLong()),
    SUSPEND(null, Type.Constant.TLong()),
    PRINT(null, Type.Constant.TString()),
}