package tech.poder.ir.data.base

import tech.poder.ir.data.base.unlinked.UnlinkedContainer
import tech.poder.ir.util.MemorySegmentBuffer

interface Container {
    companion object {
        fun newContainer(name: String): UnlinkedContainer {
            return UnlinkedContainer(name)
        }
    }

    val name: String

    fun size(): Long

    fun save(buffer: MemorySegmentBuffer)
}