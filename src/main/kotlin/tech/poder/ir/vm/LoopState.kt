package tech.poder.ir.vm

import tech.poder.ptir.PTIR

data class LoopState(var condition: PTIR.Variable, var start: Int, var end: Int)
