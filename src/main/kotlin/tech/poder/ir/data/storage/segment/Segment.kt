package tech.poder.ir.data.storage.segment

import tech.poder.ir.commands.Command
import tech.poder.ir.data.Label
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.unlinked.UnlinkedMethod
import tech.poder.ir.util.MemorySegmentBuffer
import java.util.*

interface Segment {

    fun eval(
        method: UnlinkedMethod,
        stack: Stack<Type>,
        currentVars: MutableList<Type>,
        currentIndex: Int,
        labels: MutableMap<Int, Label>
    ): Int

    fun size(): Int

    fun toBulk(storage: List<Command>)

    fun toBin(buffer: MemorySegmentBuffer)

    fun sizeBytes(): Long
}