package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Type
import java.util.*

data class BranchHolder(
    val ifBlock: Segment,
    val elseBlock: Segment?,
    val stackChanges: ArrayList<Type> = arrayListOf()
) : Segment {
    override fun eval(stack: Stack<Type>) {

    }

    override fun size(): Int {
        return ifBlock.size() + (elseBlock?.size() ?: 0)
    }

    override fun toBulk(storage: ArrayList<Instruction>) {
        ifBlock.toBulk(storage)
        elseBlock?.toBulk(storage)
    }
}