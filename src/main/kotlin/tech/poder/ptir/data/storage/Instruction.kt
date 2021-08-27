package tech.poder.ptir.data.storage

import tech.poder.ptir.commands.Command

data class Instruction(var opCode: Command, var extra: Any? = null)
