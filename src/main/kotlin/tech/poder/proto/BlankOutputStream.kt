package tech.poder.proto

import java.io.OutputStream

class BlankOutputStream(var amountWritten: Int = 0) : OutputStream() {
	companion object {
		fun makeBitVersion(): BitOutputStream {
			return BitOutputStream(BlankOutputStream())
		}
	}
	override fun write(b: Int) {
		amountWritten++
	}

	override fun write(b: ByteArray) {
		amountWritten += b.size
	}

	override fun write(b: ByteArray, off: Int, len: Int) {
		amountWritten += len
	}

}