package tech.poder.ptir.parsing.windows.imports

import tech.poder.ptir.parsing.windows.ExeFormat
import tech.poder.ptir.util.MemorySegmentBuffer


sealed interface ImportLookupTable {
    companion object {
        fun compose(reader: MemorySegmentBuffer, format: ExeFormat): ImportLookupTable {
            return if (format == ExeFormat.PE32) {
                val input = reader.readUInt()
                if (input == 0u) {
                    return NullLookup
                }
                if (input and 0x80000000u != 0u) {
                    Ordinal(input.toUShort())
                } else {
                    UnresolvedHintName(input)
                }
            } else {
                val input = reader.readULong()
                if (input == 0uL) {
                    return NullLookup
                }
                if (input and 0x8000000000000000u != 0uL) {
                    Ordinal(input.toUShort())
                } else {
                    UnresolvedHintName(input.toUInt())
                }
            }
        }
    }

    object NullLookup : ImportLookupTable

    data class UnresolvedHintName(val rva: UInt) : ImportLookupTable {
        fun resolve(reader: MemorySegmentBuffer): HintName {
            return HintName(reader.readUShort(), reader.readCString())
        }
    }

    data class Ordinal(val ordinalNumber: UShort) : ImportLookupTable

    data class HintName(val hint: UShort, val name: String) : ImportLookupTable
}

