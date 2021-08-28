package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Type
import java.util.*

data class LoopHolder(val block: Segment, val stackChanges: ArrayList<Type> = arrayListOf()) : Segment {
    override fun eval(stack: Stack<Type>) {

    }

    override fun size(): Int {
        return block.size()
    }

    override fun toBulk(storage: ArrayList<Instruction>) {
        block.toBulk(storage)
    }
}