package tech.poder.ptir.data

import tech.poder.ptir.commands.Command

data class Instruction(val opCode: Command, val extra: Any? = null)
