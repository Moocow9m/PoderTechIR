package tech.poder.ir.commands

import tech.poder.ir.data.storage.Type

enum class SysCommand(val return_: Type?, vararg val args: Type) {
    SLEEP(null, Type.Constant.TLong()),
    SUSPEND(null, Type.Constant.TLong()),
    YIELD(null),
    LOAD_LIB(Type.Constant.TByte(), Type.Constant.TString()), //boolean returned as byte
    PRINT(null, Type.Constant.TString()),
}