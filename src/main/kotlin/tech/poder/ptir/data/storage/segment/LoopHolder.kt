package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.storage.Type

data class LoopHolder(val block: MultiSegment, val stackChanges: ArrayList<Type>) : Segment {
    fun eval() {

    }
}