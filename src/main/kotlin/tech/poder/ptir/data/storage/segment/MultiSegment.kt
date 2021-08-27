package tech.poder.ptir.data.storage.segment

data class MultiSegment(val instructions: ArrayList<Segment>, val stackChange: Int = 0) : Segment {
    fun eval() {

    }
}
