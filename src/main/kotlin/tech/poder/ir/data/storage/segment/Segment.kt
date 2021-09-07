package tech.poder.ir.data.storage.segment

import tech.poder.ir.data.storage.Instruction
import tech.poder.ptir.data.Label
import tech.poder.ptir.data.Type
import tech.poder.ptir.data.base.Method
import java.util.*

interface Segment {

    fun eval(
        method: Method,
        stack: Stack<Type>,
        currentVars: MutableList<Type>,
        currentIndex: Int,
        labels: MutableMap<Int, Label>
    ): Int

    fun size(): Int

    fun toBulk(storage: MutableList<Instruction>)

}