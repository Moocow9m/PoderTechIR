package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.storage.Type
import java.util.*

data class BranchHolder(
    val ifBlock: MultiSegment,
    val elseBlock: MultiSegment?,
    val stackChanges: ArrayList<Type> = arrayListOf()
) : Segment {
    override fun eval(stack: Stack<Type>) {

    }

    override fun size(): Int {
        return ifBlock.size() + (elseBlock?.size() ?: 0)
    }
}