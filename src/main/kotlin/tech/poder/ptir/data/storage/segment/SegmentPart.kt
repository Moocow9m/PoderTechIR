package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Type
import java.util.*

data class SegmentPart(
    val data: ArrayList<Instruction> = arrayListOf(),
    val stackChanges: ArrayList<Type> = arrayListOf()
) : Segment {
    override fun eval(stack: Stack<Type>) {

    }

    override fun size(): Int {
        return data.size
    }
}
