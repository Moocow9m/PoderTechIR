package tech.poder.proto

import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

object ReadStd {

	fun readVarInt(input: InputStream): Int {
		var length = 0
		var result = 0
		var shift = 0
		var b: Int
		do {
			length++
			if (length > 5) {
				throw Exception("VarInt too long")
			}
			b = input.read()
			if (b == -1) {
				throw Exception("Unexpected end of stream")
			}
			result = result or ((b and 0x7F) shl shift)
			shift += 7

		} while (b and 0x80 != 0)
		return result
	}

	fun readZigZag(value: Int): Int {
		return value shr 1 xor -(value and 1)
	}

	fun readVInt(stream: InputStream): Int {
		return readZigZag(readVarInt(stream))
	}

	fun readVUInt(stream: InputStream): UInt {
		return readVarInt(stream).toUInt()
	}

	fun readString(stream: InputStream): String {
		return stream.readNBytes(readVUInt(stream).toInt()).decodeToString()
	}

	fun <T> readPacket(stream: InputStream, objectInstance: Any): T {
		return objectInstance::class.java.getMethod("fromBytes", InputStream::class.java)
			.invoke(objectInstance, stream) as T
	}

	fun readBoolean(stream: InputStream): Boolean {
		return stream.read() != 0
	}

	fun readAnyList(stream: InputStream): List<Any> {
		return List(readVUInt(stream).toInt()) {
			readAny(stream)
		}
	}

	fun readAny(stream: InputStream): Any {
		return when (val code =
			Packet.Types.values()[max(min(readVUInt(stream).toInt(), Packet.Types.UNKNOWN.ordinal), 0)]) {
			Packet.Types.VUINT -> readVUInt(stream)
			Packet.Types.VINT -> readVInt(stream)
			Packet.Types.STRING -> readString(stream)
			Packet.Types.PACKET -> {
				val packet = ReadStd.javaClass.classLoader.loadClass(readString(stream))
					?: throw Exception("Unknown packet class")
				val objectInstance = packet.getField("Companion").get(null)
				readPacket(stream, objectInstance)
			}
			Packet.Types.LIST -> readAnyList(stream)
			Packet.Types.BOOL -> readBoolean(stream)
			else -> throw Exception("Unknown type code: $code")
		}
	}
}