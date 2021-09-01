package tech.poder.ir.parsing.windows

data class Section(
    val name: String,
    val size: UInt,
    val address: UInt,
    val sizeOfRawData: UInt,
    val pointerToRawData: UInt,
    val pointerToRelocations: UInt,
    val numberOfRelocations: UShort,
    val flags: List<SectionFlags>,
)
