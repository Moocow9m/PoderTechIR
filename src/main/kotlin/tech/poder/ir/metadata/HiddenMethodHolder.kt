package tech.poder.ir.metadata

import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.data.storage.Type

data class HiddenMethodHolder(val id: UInt, val returnType: Type?, val args: Set<NamedType>)
