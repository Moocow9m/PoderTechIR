package tech.poder.ptir.parsing.windows.exports

data class ExportDirectory(
    val name: String,
    val startingOrdinal: UInt,
    val exportEntries: List<ExportEntry>
)
