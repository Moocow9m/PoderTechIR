package tech.poder.ir.data.storage.memory

import jdk.incubator.foreign.MemorySegment

//based off SLUB
class MemoryAllocator(memorySize: Long, private val pageSize: Long) {
    private val fragments = mutableMapOf<Int, ArrayList<Fragment>>()
    private val memory = MemorySegment.allocateNative(memorySize)


}
