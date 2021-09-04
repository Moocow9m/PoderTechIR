package tech.poder.ir.metadata

import tech.poder.ir.data.storage.NamedType

data class ObjectHolder(
	val fullName: String,
	val fields: List<NamedType>,
)
