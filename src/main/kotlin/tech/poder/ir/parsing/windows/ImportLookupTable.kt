package tech.poder.ir.parsing.windows

data class ImportLookupTable(val ordinalNumber: UShort? = null, val rva: UInt? = null) {
    fun isNull(): Boolean {
        return ordinalNumber == null && rva == null
    }

    companion object {
        fun compose(input: ULong, format: ExeFormat): ImportLookupTable {
            val ordinal = if (format == ExeFormat.PE32) {
                input and 0x80000000u != 0uL
            } else {
                input and 0x8000000000000000u != 0uL
            }
            return if (ordinal) {
                ImportLookupTable(input.toUShort())
            } else {
                ImportLookupTable(rva = input.toUInt())
            }
        }
    }
}
