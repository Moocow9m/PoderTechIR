package tech.poder.ir.data.base.linked

import tech.poder.ir.data.base.Method
import tech.poder.ir.data.base.Object
import tech.poder.ir.data.storage.NamedType

data class PublicObject(val id: UInt, val name: String, val fields: List<NamedType>, val methods: List<Method>) : Object