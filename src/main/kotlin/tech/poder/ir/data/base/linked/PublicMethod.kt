package tech.poder.ir.data.base.linked

import tech.poder.ir.commands.Command
import tech.poder.ir.data.base.Method

data class PublicMethod(
    val id: UInt,
    val name: String,
    val argsSize: Byte,
    val returns: Boolean,
    val instructions: List<Command>
) : Method
