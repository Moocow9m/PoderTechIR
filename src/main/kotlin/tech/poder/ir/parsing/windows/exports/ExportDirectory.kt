package tech.poder.ir.parsing.windows.exports

data class ExportDirectory(
    val name: String,
    val startingOrdinal: UInt,
    val exportTables: List<ExportTable>,
    val nameOrdinals: List<NameOrdinal>
)
