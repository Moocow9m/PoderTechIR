package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.storage.Type
import java.util.*

data class LoopHolder(val block: MultiSegment, val stackChanges: ArrayList<Type> = arrayListOf()) : Segment {
    override fun eval(stack: Stack<Type>) {

    }
}