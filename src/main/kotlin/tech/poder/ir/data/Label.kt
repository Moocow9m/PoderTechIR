package tech.poder.ir.data

data class Label internal constructor(val id: UShort, internal var location: Int = -2) {

	fun use(index: Int) {

		check(!isUsed()) {
			"Label already in use!"
		}

		location = index
	}

	fun isUsed(): Boolean {
		return location > -2
	}
}
