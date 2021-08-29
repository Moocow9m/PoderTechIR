package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.base.Method
import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Type
import java.util.*

data class SegmentPart(
    val data: ArrayList<Instruction> = arrayListOf()
) : Segment {
    override fun eval(method: Method, stack: Stack<Type>) {

    }

    override fun size(): Int {
        return data.size
    }

    override fun toBulk(storage: ArrayList<Instruction>) {
        storage.addAll(data)
    }
}
