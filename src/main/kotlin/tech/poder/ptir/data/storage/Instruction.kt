package tech.poder.ptir.data.storage

import tech.poder.ptir.commands.Command

data class Instruction(val opCode: Command, var extra: Any? = null)
