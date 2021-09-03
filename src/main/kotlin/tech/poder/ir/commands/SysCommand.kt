package tech.poder.ir.commands

import tech.poder.ir.data.storage.Type

enum class SysCommand(val return_: Type?, vararg val args: Type) {
    SLEEP(null, Type.Primitive.TLong()),
    SUSPEND(null, Type.Primitive.TLong()),
    YIELD(null),
    LOAD_LIB(Type.Primitive.TByte(), Type.Primitive.TString()), //boolean returned as byte
    PRINT(null, Type.Primitive.TString()),
}