package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Type

data class SegmentPart(
    val data: ArrayList<Instruction> = arrayListOf(),
    val stackChanges: ArrayList<Type> = arrayListOf()
) : Segment
