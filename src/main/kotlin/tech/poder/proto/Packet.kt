package tech.poder.proto

interface Packet {
	enum class Types {
		ENUM, PACKET, LIST, STRING, UNION, BOOL, VUINT, VINT, BYTE, UBYTE, VSHORT, VUSHORT, VLONG, VULONG, UNKNOWN
	}

	fun length(): Int {
		val os = BlankOutputStream.makeBitVersion()
		toBytes(os)
		os.close()
		return (os.realOutputStream as BlankOutputStream).amountWritten
	}

	fun toBytes(stream: BitOutputStream)
}