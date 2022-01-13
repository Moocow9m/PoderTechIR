package tech.poder.proto

import java.io.OutputStream

class BitOutputStream(private val realOutputStream: OutputStream) : OutputStream() {
	var bitPos = 0
	var bitBuffer = 0

	fun writeBit(bit: Boolean) {
		bitBuffer = bitBuffer shl 1
		if (bit) {
			bitBuffer = bitBuffer or 1
		}
		bitPos++
		if (bitPos == 8) {
			realOutputStream.write(bitBuffer)
			bitPos = 0
			bitBuffer = 0
		}
	}

	override fun write(b: Int) {
		if (bitPos == 0) {
			realOutputStream.write(b)
		} else {
			val remainingSpace = 8 - bitPos
			val mask = (bitBuffer shl remainingSpace) - 1
			val newBitBuffer = b and mask
			realOutputStream.write(newBitBuffer)
			bitBuffer = newBitBuffer
		}
	}

	override fun flush() {
		realOutputStream.flush()
	}

	override fun close() {
		if (bitPos > 0) {
			realOutputStream.write(bitBuffer)
		}
		bitPos = 0
		bitBuffer = 0
		flush()
		realOutputStream.close()
		super.close()
	}
}