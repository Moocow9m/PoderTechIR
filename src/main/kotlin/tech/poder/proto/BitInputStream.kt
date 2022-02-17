package tech.poder.proto

import java.io.EOFException
import java.io.InputStream

class BitInputStream(private val realInputStream: InputStream) : InputStream() {
	var bitPos = 0
	var bitBuffer = 0

	fun skipSpareBits() {
		bitPos = 0
	}

	fun readBit(): Boolean {
		if (bitPos == 0) {
			bitBuffer = readRead()
			bitPos = 8
		}
		bitPos--
		return (bitBuffer and (1 shl bitPos)) != 0
	}

	override fun read(): Int {
		return if (bitPos == 0) {
			readRead()
		} else {
			var result = 0
			repeat(8) {
				result = result shl 1
				if (readBit()) {
					result = result or 1
				}
			}
			result
		}
	}

	private fun readRead(): Int {
		val read = realInputStream.read()
		if (read == -1) {
			throw EOFException()
		}
		return read
	}

	override fun close() {
		realInputStream.close()
		super.close()
	}
}