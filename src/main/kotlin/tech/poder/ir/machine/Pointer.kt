package tech.poder.ir.machine

import tech.poder.ir.machine.amd64.RegisterName
import tech.poder.ir.machine.amd64.RegisterSize

@JvmInline
value class Pointer(private val address: Any) {
	override fun toString(): String {
		return when (address) {
			is String -> {
				"[${address}]"
			}
			is Number -> {
				"[0x${address.toLong().toString(16)}]"
			}
			is Pair<*, *> -> {
				if (address.first is RegisterName && address.second is RegisterSize) {
					"[${RegisterName.realName(address.first as RegisterName, address.second as RegisterSize)}]"
				} else {
					error("Unsupported type: ${address::class.java.name}")
				}
			}
			else -> error("Unsupported type: ${address::class.java.name}")
		}
	}
}