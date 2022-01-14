package tech.poder.proto

interface Packet {
	enum class Types {
		ENUM, PACKET, LIST, STRING, UNION, BOOL, VUINT, VINT, BYTE, UBYTE, VSHORT, VUSHORT, VLONG, VULONG, UNKNOWN;
		companion object {
			val typesToBin = mapOf(
				UNION to listOf(false, false, false),
				STRING to listOf(false, false, true),
				VUINT to listOf(false, true, true),
				UBYTE to listOf(false, true, false, false),
				BYTE to listOf(false, true, false, true),
				PACKET to listOf(true, false, false, false),
				VULONG to listOf(true, false, false, true),
				LIST to listOf(true, false, true, false),
				ENUM to listOf(true, false, true, true),
				BOOL to listOf(true, true, false, true),
				VINT to listOf(true, true, true, false),
				VLONG to listOf(true, true, true, true),
				VUSHORT to listOf(true, true, false, false, false),
				VSHORT to listOf(true, true, false, false, true),
			)
			val binToTypes = mapOf(
				listOf(false, false, false) to UNION,
				listOf(false, false, true) to STRING,
				listOf(false, true, true) to VUINT,
				listOf(false, true, false, false) to UBYTE,
				listOf(false, true, false, true) to BYTE,
				listOf(true, false, false, false) to PACKET,
				listOf(true, false, false, true) to VULONG,
				listOf(true, false, true, false) to LIST,
				listOf(true, false, true, true) to ENUM,
				listOf(true, true, false, true) to BOOL,
				listOf(true, true, true, false) to VINT,
				listOf(true, true, true, true) to VLONG,
				listOf(true, true, false, false, false) to VUSHORT,
				listOf(true, true, false, false, true) to VSHORT,
			)
			const val MAX_BITS: Int = 6
		}
	}

	companion object {
		const val countHuffmanFrequency = false
		val frequencyList by lazy {
			mutableMapOf<String, Int>()
		}
	}

	fun length(): ULong {
		val os = BlankOutputStream()
		toBytes(os)
		os.close()
		return os.amountWritten
	}

	fun toBytes(stream: BitOutputStream)
}