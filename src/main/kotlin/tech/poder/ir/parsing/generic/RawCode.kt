package tech.poder.ir.parsing.generic

import tech.poder.ir.data.storage.Instruction

sealed interface RawCode {
    data class Unprocessed(val data: List<ByteArray>)

    data class Processed(val data: List<Instruction>)
}