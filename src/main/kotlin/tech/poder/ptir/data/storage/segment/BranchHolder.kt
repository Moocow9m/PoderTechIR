package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.storage.Type

data class BranchHolder(val ifBlock: MultiSegment, val elseBlock: MultiSegment?, val stackChanges: ArrayList<Type>) :
    Segment {
    fun eval() {

    }
}