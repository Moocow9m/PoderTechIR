package tech.poder.proto

interface Packet {
	enum class Types {
		ENUM, PACKET, LIST, STRING, UNION, BOOL, VUINT, VINT, BYTE, UBYTE, VSHORT, VUSHORT, VLONG, VULONG, UNKNOWN;

		companion object {
			val typesToBin = mapOf(
				STRING to listOf(false, false, false),
				VINT to listOf(false, false, true),
				VUINT to listOf(false, true, true),
				VSHORT to listOf(false, true, false, false),
				UBYTE to listOf(false, true, false, true),
				BYTE to listOf(true, false, false, false),
				PACKET to listOf(true, false, false, true),
				VULONG to listOf(true, false, true, false),
				LIST to listOf(true, false, true, true),
				ENUM to listOf(true, true, false, true),
				BOOL to listOf(true, true, true, false),
				VLONG to listOf(true, true, true, true),
				UNION to listOf(true, true, false, false, false),
				VUSHORT to listOf(true, true, false, false, true),
			)
			val binToTypes = mapOf(
				listOf(false, false, false) to STRING,
				listOf(false, false, true) to VINT,
				listOf(false, true, true) to VUINT,
				listOf(false, true, false, false) to VSHORT,
				listOf(false, true, false, true) to UBYTE,
				listOf(true, false, false, false) to BYTE,
				listOf(true, false, false, true) to PACKET,
				listOf(true, false, true, false) to VULONG,
				listOf(true, false, true, true) to LIST,
				listOf(true, true, false, true) to ENUM,
				listOf(true, true, true, false) to BOOL,
				listOf(true, true, true, true) to VLONG,
				listOf(true, true, false, false, false) to UNION,
				listOf(true, true, false, false, true) to VUSHORT,
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