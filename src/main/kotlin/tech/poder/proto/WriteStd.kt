package tech.poder.proto

object WriteStd {

	fun writeVarInt(out: BitOutputStream, int: Int) {
		var value = int
		while (true) {
			if (value and 0x7F == value) {
				out.write(value)
				return
			}
			out.write((value and 0x7F or 0x80))
			value = value ushr 7
		}
	}

	fun writeVarLong(out: BitOutputStream, long: Long) {
		var value = long
		while (true) {
			if (value and 0x7F == value) {
				out.write(value.toInt())
				return
			}
			out.write((value and 0x7F or 0x80).toInt())
			value = value ushr 7
		}
	}

	fun writeZigZag(value: Int): Int {
		return (value shl 1) xor (value shr 31)
	}

	fun writeZigZag(value: Long): Long {
		return (value shl 1) xor (value shr 31)
	}

	fun writeVInt(stream: BitOutputStream, int: Int) {
		writeVarInt(stream, writeZigZag(int))
	}

	fun writeVUInt(stream: BitOutputStream, int: UInt) {
		writeVarInt(stream, int.toInt())
	}

	fun writeVLong(stream: BitOutputStream, long: Long) {
		writeVarLong(stream, writeZigZag(long))
	}

	fun writeVULong(stream: BitOutputStream, long: ULong) {
		writeVarLong(stream, long.toLong())
	}

	fun writeString(stream: BitOutputStream, string: String) {
		val bytes = string.encodeToByteArray()
		writeVUInt(stream, bytes.size.toUInt())
		stream.write(bytes)
	}

	fun writePacket(stream: BitOutputStream, packet: Packet) {
		packet.toBytes(stream)
	}

	fun writeAnyList(stream: BitOutputStream, list: List<*>) {
		writeVUInt(stream, list.size.toUInt())
		list.forEach {
			writeAny(stream, it!!)
		}
	}

	fun writeBoolean(stream: BitOutputStream, bool: Boolean) {
		stream.writeBit(bool)
	}

	fun writeList(stream: BitOutputStream, list: List<*>) {//write shortcut(on read, type is known)
		writeVUInt(stream, list.size.toUInt())
		list.forEach {
			writeAnyNoPrefix(stream, it!!)
		}
	}

	fun writeAnyNoPrefix(stream: BitOutputStream, any: Any) {//write shortcut(on read, type is known)
		when (any) {
			is UInt -> writeVUInt(stream, any)
			is Packet -> writePacket(stream, any)
			is String -> writeString(stream, any)
			is List<*> -> writeList(stream, any)
			is Boolean -> writeBoolean(stream, any)
			else -> error("Unsupported type: ${any::class.simpleName}")
		}
	}

	fun writeAny(stream: BitOutputStream, any: Any) {
		when (any) {
			is Enum<*> -> {
				writeVUInt(stream, Packet.Types.ENUM.ordinal.toUInt())
				writeVUInt(stream, any.ordinal.toUInt())
			}
			is UInt -> {
				writeVUInt(stream, Packet.Types.VUINT.ordinal.toUInt())
				writeVUInt(stream, any)
			}
			is Int -> {
				writeVUInt(stream, Packet.Types.VINT.ordinal.toUInt())
				writeVInt(stream, any)
			}
			is Byte -> {
				writeVUInt(stream, Packet.Types.BYTE.ordinal.toUInt())
				stream.write(any.toInt())
			}
			is UByte -> {
				writeVUInt(stream, Packet.Types.UBYTE.ordinal.toUInt())
				stream.write(any.toInt())
			}
			is Short -> {
				writeVUInt(stream, Packet.Types.VSHORT.ordinal.toUInt())
				writeVInt(stream, any.toInt())
			}
			is UShort -> {
				writeVUInt(stream, Packet.Types.VUSHORT.ordinal.toUInt())
				writeVUInt(stream, any.toUInt())
			}
			is Long -> {
				writeVUInt(stream, Packet.Types.VLONG.ordinal.toUInt())
				writeVLong(stream, any)
			}
			is ULong -> {
				writeVUInt(stream, Packet.Types.VULONG.ordinal.toUInt())
				writeVULong(stream, any)
			}
			is Packet -> {
				writeVUInt(stream, Packet.Types.PACKET.ordinal.toUInt())
				writeString(stream, any::class.java.name)
				writePacket(stream, any)
			}
			is String -> {
				writeVUInt(stream, Packet.Types.STRING.ordinal.toUInt())
				writeString(stream, any)
			}
			is List<*> -> {
				writeVUInt(stream, Packet.Types.LIST.ordinal.toUInt())
				writeAnyList(stream, any)
			}
			is Boolean -> {
				writeVUInt(stream, Packet.Types.BOOL.ordinal.toUInt())
				writeBoolean(stream, any)
			}
			else -> error("Unsupported type: ${any::class.simpleName}")
		}
	}
}