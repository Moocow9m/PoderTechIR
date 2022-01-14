package tech.poder.proto

import java.io.OutputStream

open class BitOutputStream(internal val realOutputStream: OutputStream) : OutputStream() {
	var bitPos = 0
	var bitBuffer = 0

	fun flushSpareBits() {
		if (bitPos > 0) {
			bitBuffer = bitBuffer shl (8 - bitPos)
			realOutputStream.write(bitBuffer)
			bitBuffer = 0
			bitPos = 0
		}
	}

	open fun writeBit(bit: Boolean) {
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
			writeBit(b and 128 != 0)
			writeBit(b and 64 != 0)
			writeBit(b and 32 != 0)
			writeBit(b and 16 != 0)
			writeBit(b and 8 != 0)
			writeBit(b and 4 != 0)
			writeBit(b and 2 != 0)
			writeBit(b and 1 != 0)
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