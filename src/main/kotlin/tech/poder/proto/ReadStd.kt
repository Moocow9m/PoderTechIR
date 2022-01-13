package tech.poder.proto

import kotlin.math.max
import kotlin.math.min

object ReadStd {

	fun readVarInt(input: BitInputStream): Int {
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

	fun readVarLong(input: BitInputStream): Long {
		var length = 0L
		var result = 0L
		var shift = 0
		var b: Long
		do {
			length++
			if (length > 5) {
				throw Exception("VarInt too long")
			}
			b = input.read().toLong()
			if (b == -1L) {
				throw Exception("Unexpected end of stream")
			}
			result = result or ((b and 0x7F) shl shift)
			shift += 7

		} while (b and 0x80 != 0L)
		return result
	}

	fun readZigZag(value: Int): Int {
		return value shr 1 xor -(value and 1)
	}

	fun readZigZag(value: Long): Long {
		return value shr 1 xor -(value and 1)
	}

	fun readVInt(stream: BitInputStream): Int {
		return readZigZag(readVarInt(stream))
	}

	fun readVUInt(stream: BitInputStream): UInt {
		return readVarInt(stream).toUInt()
	}

	fun readVLong(stream: BitInputStream): Long {
		return readZigZag(readVarLong(stream))
	}

	fun readVULong(stream: BitInputStream): ULong {
		return readVarLong(stream).toULong()
	}

	fun readString(stream: BitInputStream): String {
		return stream.readNBytes(readVUInt(stream).toInt()).decodeToString()
	}

	fun <T> readPacket(stream: BitInputStream, objectInstance: Any): T {
		return objectInstance::class.java.getMethod("fromBytes", BitInputStream::class.java)
			.invoke(objectInstance, stream) as T
	}

	fun readBoolean(stream: BitInputStream): Boolean {
		return stream.readBit()
	}

	fun readAnyList(stream: BitInputStream): List<Any> {
		return List(readVUInt(stream).toInt()) {
			readAny(stream)
		}
	}

	fun readAny(stream: BitInputStream): Any {
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
			Packet.Types.ENUM -> TODO("ENUM")
			Packet.Types.UNION -> TODO("UNION")
			Packet.Types.BYTE -> stream.read().toByte()
			Packet.Types.UBYTE -> stream.read().toUByte()
			Packet.Types.VSHORT -> readVInt(stream).toShort()
			Packet.Types.VUSHORT -> readVUInt(stream).toUShort()
			Packet.Types.VLONG -> readVLong(stream)
			Packet.Types.VULONG -> readVULong(stream)
			Packet.Types.UNKNOWN -> throw Exception("Unknown type code: $code")
		}
	}
}