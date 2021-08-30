package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.base.Method
import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Label
import tech.poder.ptir.data.storage.Type
import java.util.*

interface Segment {

    fun eval(
        method: Method,
        stack: Stack<Type>,
        currentVars: Array<Type?>,
        currentIndex: Int,
        labels: MutableMap<Int, Label>
    ): Int

    fun size(): Int

    fun toBulk(storage: ArrayList<Instruction>)

}