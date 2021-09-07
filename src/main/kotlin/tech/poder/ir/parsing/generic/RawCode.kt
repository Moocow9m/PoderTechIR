package tech.poder.ir.parsing.generic

import tech.poder.ir.commands.Command

sealed interface RawCode {

    data class Unprocessed(val name: String, val data: List<Command>, val fileLocation: Int)

    data class Processed(val name: String, val data: List<Command>)

}