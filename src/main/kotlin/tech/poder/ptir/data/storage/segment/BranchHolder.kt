package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.base.Method
import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Type
import java.util.*

data class BranchHolder(
    val ifBlock: Segment,
    val elseBlock: Segment?
) : Segment {
    override fun eval(method: Method, stack: Stack<Type>) {
        ifBlock.eval(method, stack)
        elseBlock?.eval(method, stack)
    }

    override fun size(): Int {
        return ifBlock.size() + (elseBlock?.size() ?: 0)
    }

    override fun toBulk(storage: ArrayList<Instruction>) {
        ifBlock.toBulk(storage)
        elseBlock?.toBulk(storage)
    }
}