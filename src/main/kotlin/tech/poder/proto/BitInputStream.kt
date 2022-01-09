package tech.poder.proto

import java.io.InputStream

class BitInputStream(private val realInputStream: InputStream): InputStream() {
	var bitPos = 0
	var bitBuffer = 0

	fun readBit(): Boolean {
		TODO()
	}

	override fun read(): Int {
		return if (bitPos == 0) {
			realInputStream.read()
		} else {
			TODO()
		}
	}

	override fun close() {
		realInputStream.close()
		super.close()
	}
}