package tech.poder.ptir.parsing.windows

import tech.poder.ptir.parsing.windows.flags.SectionFlags

data class Section(
    val name: String,
    val size: UInt,
    val address: UInt,
    val sizeOfRawData: UInt,
    val pointerToRawData: UInt,
    val pointerToRelocations: UInt,
    val numberOfRelocations: UShort,
    val flags: List<SectionFlags>,
) {
    val range = address..(size + address)
}
