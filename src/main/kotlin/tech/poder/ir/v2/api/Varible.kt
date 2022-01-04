package tech.poder.ir.v2.api

@JvmInline
value class Varible(internal val id: UInt = last++) {
	companion object {
		var last = 0u
	}
}
