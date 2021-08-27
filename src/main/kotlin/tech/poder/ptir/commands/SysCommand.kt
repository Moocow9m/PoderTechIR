package tech.poder.ptir.commands

import tech.poder.ptir.data.storage.Type

enum class SysCommand(val return_: Type?, vararg val args: Type) {
    SLEEP(null, Type.Constant.TLong()),
    SUSPEND(null, Type.Constant.TLong()),
    LOAD_DLL(Type.Constant.TByte(), Type.Constant.TString()), //boolean returned as byte
    PRINT(null, Type.Constant.TString()),
}