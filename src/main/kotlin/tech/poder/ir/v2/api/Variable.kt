package tech.poder.ir.v2.api

@JvmInline
value class Variable(internal val id: UInt = last++) {
	companion object {
		var last = 0u
	}
}
