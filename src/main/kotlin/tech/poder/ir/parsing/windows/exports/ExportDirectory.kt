package tech.poder.ir.parsing.windows.exports

data class ExportDirectory(
	val name: String,
	val startingOrdinal: UInt,
	val exportEntries: List<ExportEntry>
)
