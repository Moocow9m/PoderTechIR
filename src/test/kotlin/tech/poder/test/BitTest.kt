package tech.poder.test

import org.junit.jupiter.api.Test
import tech.poder.proto.BitInputStream
import tech.poder.proto.BitOutputStream
import java.io.ByteArrayOutputStream

class BitTest {
	@Test
	fun readWriteTestBasic() {
		val outputStream = ByteArrayOutputStream(1024)
		val output = BitOutputStream(outputStream)
		output.writeBit(true)
		output.writeBit(false)
		output.flushSpareBits()
		output.write(22)

		output.close()
		val nis = BitInputStream(outputStream.toByteArray().inputStream())
		assert(nis.readBit())
		assert(!nis.readBit())
		nis.skipSpareBits()
		assert(nis.read() == 22)
		nis.close()
	}

	@Test
	fun readWriteTestExtra() {
		val outputStream = ByteArrayOutputStream(3)
		val output = BitOutputStream(outputStream)
		output.writeBit(true)
		output.writeBit(false)
		output.write(22)
		output.flushSpareBits()

		output.close()
		val nis = BitInputStream(outputStream.toByteArray().inputStream())
		assert(nis.readBit())
		assert(!nis.readBit())
		assert(nis.read() == 22)
		nis.skipSpareBits()
		nis.close()
	}
}