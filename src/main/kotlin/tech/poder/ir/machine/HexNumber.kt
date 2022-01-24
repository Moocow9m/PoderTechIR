package tech.poder.ir.machine

@JvmInline
value class HexNumber(val num: Number) {
	override fun toString(): String = "0x${num}"
}