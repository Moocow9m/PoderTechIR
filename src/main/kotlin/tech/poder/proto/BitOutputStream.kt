package tech.poder.proto

import java.io.OutputStream

open class BitOutputStream(internal val realOutputStream: OutputStream) : OutputStream() {
	var bitPos = 0
	var bitBuffer = 0

	fun flushSpareBits() {
		if (bitPos > 0) {
			bitBuffer = bitBuffer shl (8 - bitPos)
			realWrite(bitBuffer)
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
			realWrite(bitBuffer)
			bitPos = 0
			bitBuffer = 0
		}
	}

	fun write(b: Byte) {
		write(b.toInt())
	}

	override fun write(b: Int) {
		if (bitPos == 0) {
			realWrite(b)
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

	private fun realWrite(byte: Int) {
		realOutputStream.write(byte)
	}


	override fun close() {
		if (bitPos > 0) {
			realWrite(bitBuffer)
		}
		bitPos = 0
		bitBuffer = 0
		flush()
		realOutputStream.close()
		super.close()
	}
}