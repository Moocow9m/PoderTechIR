package tech.poder.ir.api

import tech.poder.ptir.PTIR

@JvmInline
value class Struct(val types: Array<PTIR.FullType>) {
	override fun toString(): String {
		return "Struct(types=[${types.joinToString(", ")}])"
	}
}
