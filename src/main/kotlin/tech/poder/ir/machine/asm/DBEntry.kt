package tech.poder.ir.machine.asm

import tech.poder.ir.machine.Pointer

data class DBEntry(val name: String, val value: String) {
	companion object {
		fun construct(name: String, entry: Any): DBEntry {
			val result = when(entry) {
				is String -> "\'$entry\',0".replace("\n", "',0xA,'").replace("\r", "',0xD,'").replace(",'',", ",")
				else -> entry.toString()
			}
			return DBEntry(name, result)
		}
	}

	val pointer = Pointer(name)

	override fun toString(): String {
		return "$name: db $value"
	}
}