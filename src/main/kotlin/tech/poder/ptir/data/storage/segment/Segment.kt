package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.base.Method
import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Type
import java.util.*

interface Segment {

    fun eval(method: Method, stack: Stack<Type>)

    fun size(): Int

    fun toBulk(storage: ArrayList<Instruction>)

}