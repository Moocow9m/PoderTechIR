package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.base.Method
import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Type
import java.util.*

data class LoopHolder(val block: Segment) : Segment {
    override fun eval(method: Method, stack: Stack<Type>) {
        //todo determine how stack should process this...
        block.eval(method, stack)
    }

    override fun size(): Int {
        return block.size()
    }

    override fun toBulk(storage: ArrayList<Instruction>) {
        block.toBulk(storage)
    }
}