package tech.poder.ir.data.base.linked

import tech.poder.ir.data.Type

data class PrivateObject(val id: UInt, val fields: List<Type>, val methods: List<PrivateMethod>)