package tech.poder.proto

interface Packet {
	enum class Types {
		ENUM, PACKET, LIST, STRING, UNION, BOOL, VUINT, VINT, BYTE, UBYTE, VSHORT, VUSHORT, VLONG, VULONG, UNKNOWN
	}

	fun length(): ULong {
		val os = BlankOutputStream()
		toBytes(os)
		os.close()
		return os.amountWritten
	}

	fun toBytes(stream: BitOutputStream)
}