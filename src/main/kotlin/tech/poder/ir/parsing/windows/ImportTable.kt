package tech.poder.ir.parsing.windows

data class ImportTable(
    val lookupTableRVA: UInt,
    val timeStamp: UInt,
    val forwardChain: UInt,
    val name: String,
    val addressTableRVA: UInt
) {

    fun isNull(): Boolean {
        return lookupTableRVA == 0u && timeStamp == 0u && forwardChain == 0u && name.isBlank() && addressTableRVA == 0u
    }

}