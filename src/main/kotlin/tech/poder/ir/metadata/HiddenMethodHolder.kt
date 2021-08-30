package tech.poder.ir.metadata

import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.data.storage.Type

data class HiddenMethodHolder(val id: UInt, val returnType: Type?, val args: Set<NamedType>) {
    init {
        args.forEach {
            val t = it.type
            if (t is Type.Constant) {
                t.constant = false
            }
        }
        if (returnType is Type.Constant) {
            returnType.constant = false
        }
    }
}
