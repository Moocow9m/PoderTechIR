package tech.poder.ptir.parsing.windows.imports

data class ImportTable(
    val lookupTableRVA: List<ImportLookupTable>,
    val forwardChain: UInt,
    val name: String
) {

    fun isNull(): Boolean {
        return lookupTableRVA.isEmpty() && forwardChain == 0u && name.isBlank()
    }

}