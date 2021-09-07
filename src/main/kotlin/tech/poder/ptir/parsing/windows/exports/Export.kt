package tech.poder.ptir.parsing.windows.exports

sealed interface Export {
    data class ExportAddress(val exportRVA: UInt) : Export
    data class RefFunctionName(val exportRVA: String) : Export //Used for exporting another image's function
}
