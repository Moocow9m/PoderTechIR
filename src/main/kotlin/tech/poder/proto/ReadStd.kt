package tech.poder.proto

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
		return if (Packet.countHuffmanFrequency) {
			stream.readNBytes(readVUInt(stream).toInt()).decodeToString().let { s ->
				s.forEach {
					val key = "Read_String_${it}"
					Packet.frequencyList[key] = Packet.frequencyList.getOrDefault(key, 0) + 1
				}
				s
			}
		} else {
			stream.readNBytes(readVUInt(stream).toInt()).decodeToString()
		}
	}

	fun <T> readPacket(stream: BitInputStream, objectInstance: Any): T {
		return objectInstance::class.java.getMethod("fromBytes", BitInputStream::class.java)
			.invoke(objectInstance, stream) as T
	}

	fun readBoolean(stream: BitInputStream): Boolean {
		return stream.readBit()
	}

	fun readHuffman(stream: BitInputStream, targetMap: Map<List<Boolean>, *>, maxDepth: Int): Any {
		val next = mutableListOf<Boolean>()

		while (!targetMap.containsKey(next) && next.size < maxDepth) {
			next.add(stream.readBit())
		}

		return targetMap[next]!!
	}

	fun readAnyList(stream: BitInputStream): List<Any> {
		return List(readVUInt(stream).toInt()) {
			readAny(stream)
		}
	}

	fun readAny(stream: BitInputStream): Any {
		val code = readHuffman(stream, Packet.Types.binToTypes, Packet.Types.MAX_BITS) as Packet.Types
		if (Packet.countHuffmanFrequency) {
			val key = "ReadSTD_ANY_${code}"
			Packet.frequencyList[key] = Packet.frequencyList.getOrDefault(key, 0) + 1
		}
		return when (code) {
			Packet.Types.VUINT -> readVUInt(stream)
			Packet.Types.VINT -> readVInt(stream)
			Packet.Types.STRING -> readString(stream)
			Packet.Types.PACKET -> {
				TODO("Remove this")
				/*val packet = ReadStd.javaClass.classLoader.loadClass(readString(stream))
					?: throw Exception("Unknown packet class")
				val objectInstance = packet.getField("Companion").get(null)
				readPacket(stream, objectInstance)*/
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