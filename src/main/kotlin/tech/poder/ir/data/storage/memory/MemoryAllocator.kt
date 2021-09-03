package tech.poder.ir.data.storage.memory

import jdk.incubator.foreign.MemoryAccess
import jdk.incubator.foreign.MemorySegment
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.math.ceil
import kotlin.math.floor

//based off SLUB
class MemoryAllocator(memorySize: Long, private val pageSize: Long = 4_096) { //todo per process tracking
    private val fragments = ConcurrentSkipListSet<Fragment>() //todo per_cpu maintain, mass delete on process death
    private var lastFrag = 0
    private val memory = MemorySegment.allocateNative(memorySize)
    private val objSize = floor(pageSize / 8.0).toInt()

    fun offsetPerCpu(coreCount: Int): Long {
        return floor(memory.byteSize() / coreCount.toDouble()).toLong()
    }

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
            selectedFrag.lock()
            fragments.add(selectedFrag)
        }

        val segments = List(amount) {
            selectedFrag.nextFree()
        }

        val mem = AllocatedMemory(segments, selectedFrag)

        selectedFrag.unlock()
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
