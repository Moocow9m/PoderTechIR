package tech.poder.ir.metadata

import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.data.storage.Type
import kotlin.reflect.KClass

data class MethodHolder(
	val fullName: String,
	val returnType: KClass<out Type>?,
	val args: Set<NamedType>,
)
