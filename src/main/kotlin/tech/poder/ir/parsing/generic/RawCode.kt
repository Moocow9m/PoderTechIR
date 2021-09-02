package tech.poder.ir.parsing.generic

import tech.poder.ir.data.storage.Instruction

sealed interface RawCode {
    data class Unprocessed(val name: String, val data: List<Instruction>, val fileLocation: Int)

    data class Processed(val name: String, val data: List<Instruction>)
}