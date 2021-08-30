package tech.poder.ir.data.storage

import tech.poder.ir.commands.Command

data class Instruction(var opCode: Command, var extra: Any? = null)
