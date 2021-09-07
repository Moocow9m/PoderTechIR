package tech.poder.ptir.parsing.windows

data class DataDirectories(
    val virtualAddress: UInt,
    val size: UInt,
) {
    val range = virtualAddress..(virtualAddress + size)
}