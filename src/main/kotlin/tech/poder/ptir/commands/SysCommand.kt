package tech.poder.ptir.commands

import tech.poder.ir.data.storage.Type

enum class SysCommand(val return_: Type?, vararg val args: Type) {
    /*SLEEP(null, Type.Primitive.Long()),
    SUSPEND(null, Type.Primitive.Long()),
    YIELD(null),
    LOAD_LIB(Type.Primitive.Byte(), Type.Primitive.String()), //boolean returned as byte
    PRINT(null, Type.Primitive.String()),*/
}