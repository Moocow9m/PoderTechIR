package tech.poder.ir.parsing.windows

data class DataDirectories(
    val virtualAddress: UInt,
    val size: UInt,
) {
    val range = virtualAddress..(virtualAddress + size)
}