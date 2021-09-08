package tech.poder.ir.data.base.linked

import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Object

data class PrivateObject(val id: UInt, val fields: List<Type>, val methods: List<PrivateMethod>) : Object