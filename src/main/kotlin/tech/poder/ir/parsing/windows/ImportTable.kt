package tech.poder.ir.parsing.windows

data class ImportTable(
    val lookupTableRVA: ImportLookupTable,
    val forwardChain: UInt,
    val name: String
) {

    fun isNull(): Boolean {
        return lookupTableRVA.isNull() && forwardChain == 0u && name.isBlank()
    }

}