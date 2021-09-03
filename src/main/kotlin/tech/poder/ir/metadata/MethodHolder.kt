package tech.poder.ir.metadata

import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.data.storage.Type

data class MethodHolder(val fullName: String, val returnType: Type?, val args: Set<NamedType>) {
    init {
        args.forEach {
            val t = it.type
            if (t is Type.Primitive) {
                t.isConstant = false
            }
        }
        if (returnType is Type.Primitive) {
            returnType.isConstant = false
        }
    }
}
