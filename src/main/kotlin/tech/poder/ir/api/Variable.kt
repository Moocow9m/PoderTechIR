package tech.poder.ir.api

@JvmInline
value class Variable(internal val id: UInt = last++) {
	companion object {
		var last = 1u
		val ARGS = Variable(0u)
	}
}
