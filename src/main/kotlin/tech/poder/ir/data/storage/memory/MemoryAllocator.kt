package tech.poder.ir.data.storage.memory

//based off SLUB
class MemoryAllocator(val memorySize: Long, private val pageSize: Long = 1024) {
    private val fragments = mutableMapOf<Int, ArrayList<Fragment>>()


}
