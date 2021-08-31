package tech.poder.ir.data.storage.memory

import jdk.incubator.foreign.MemoryAccess
import jdk.incubator.foreign.MemorySegment
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.math.ceil
import kotlin.math.floor

//based off SLUB
class MemoryAllocator(memorySize: Long, private val pageSize: Long) {
    private val fragments = ConcurrentSkipListSet<Fragment>()
    private var lastFrag = 0
    private val memory = MemorySegment.allocateNative(memorySize)
    private val objSize = floor(pageSize / 8.0).toInt()

    fun alloc(size: Long): AllocatedMemory {
        val amount = ceil(size / objSize.toDouble()).toInt()
        var selectedFrag = fragments.filter { it.state != State.FULL }.firstOrNull {
            it.lock()

            if (it.remaining() >= amount) {
                true
            } else {
                it.unlock()
                false
            }
        }
        if (selectedFrag == null) {
            selectedFrag = Fragment(0, (pageSize * lastFrag++), pageSize, objSize)
            fragments.add(selectedFrag)
            selectedFrag.lock()
        }

        val segments = ArrayList<Int>()
        val mem = AllocatedMemory(segments, selectedFrag)
        repeat(amount) {
            segments.add(selectedFrag.nextFree())
        }
        return mem
    }

    fun read(location: Long): Byte {
        return MemoryAccess.getByteAtOffset(memory, location)
    }

    fun readInt(location: Long): Int {
        return MemoryAccess.getIntAtOffset(memory, location)
    }

    fun write(location: Long, value: Byte) {
        MemoryAccess.setByteAtOffset(memory, location, value)
    }

    fun write(location: Long, value: Int) {
        MemoryAccess.setIntAtOffset(memory, location, value)
    }

    fun free(allocatedMemory: AllocatedMemory) {
        allocatedMemory.close()
    }

}
