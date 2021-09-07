package tech.poder.ir.parsing.generic

data class RawCodeFile(
	val os: OS,
	val arch: Arch,
	val startCode: Int,
	val rawCode: MutableList<RawCode>,
)
