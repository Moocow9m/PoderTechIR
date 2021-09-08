package tech.poder.ir.data.base.linked

import tech.poder.ir.commands.Command
import tech.poder.ir.data.base.Method

data class PrivateMethod(val id: UInt, val argsSize: Byte, val returns: Boolean, val instructions: List<Command>) :
    Method
