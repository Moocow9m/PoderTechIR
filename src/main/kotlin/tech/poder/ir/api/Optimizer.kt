package tech.poder.ir.api

import tech.poder.ir.data.storage.segment.*

interface Optimizer { //TODO add method and object visit methods... maybe

    fun visitSegment(segment: Segment) {
        when (segment) {
            is MultiSegment -> {
                visit(segment)
                segment.instructions.forEach {
                    visitSegment(it)
                }
            }
            is SegmentPart -> visit(segment)
            is LoopHolder -> {
                visit(segment)
                visitSegment(segment.block)
            }
            is BranchHolder -> {
                visit(segment)
                visitSegment(segment.ifBlock)
                if (segment.elseBlock != null) {
                    visitSegment(segment.elseBlock)
                }
            }
        }
    }

    fun visit(multiSegment: MultiSegment) {}

    fun visit(segmentPart: SegmentPart)

    fun visit(loopHolder: LoopHolder) {}

    fun visit(branchHolder: BranchHolder) {}
}