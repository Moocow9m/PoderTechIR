package tech.poder.proto

class BlankOutputStream(var amountWritten: ULong = 0uL) : BitOutputStream(nullOutputStream()) {
	override fun writeBit(bit: Boolean) {
		amountWritten++
	}

	override fun write(b: Int) {
		amountWritten += 8uL
	}

	override fun write(b: ByteArray) {
		amountWritten += b.size.toULong() * 8uL
	}

	override fun write(b: ByteArray, off: Int, len: Int) {
		amountWritten += len.toULong() * 8uL
	}

}