package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.storage.Instruction

data class BranchHolder(val instructions: ArrayList<Instruction>, var stackChange: Int = 0) : Segment {
    fun eval() {

    }
}