package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.storage.Type
import java.util.*

interface Segment {
    fun eval(stack: Stack<Type>)

    fun size(): Int
}