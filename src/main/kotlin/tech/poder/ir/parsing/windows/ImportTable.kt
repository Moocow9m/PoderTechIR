package tech.poder.ir.parsing.windows

data class ImportTable(
    val lookupTableRVA: UInt,
    val timeStamp: UInt,
    val forwardChain: UInt,
    val nameRVA: UInt,
    val addressTableRVA: UInt
) {

    fun isNull(): Boolean {
        return lookupTableRVA == 0u && timeStamp == 0u && forwardChain == 0u && nameRVA == 0u && addressTableRVA == 0u
    }

}