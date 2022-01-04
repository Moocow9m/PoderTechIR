package tech.poder.ir.vm

import tech.poder.ptir.PTIR

data class LoopState(var condition: PTIR.Variable? = null, var start: Int = 0, var end: Int = 0)
