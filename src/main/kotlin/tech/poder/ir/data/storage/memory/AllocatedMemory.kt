package tech.poder.ir.data.storage.memory

data class AllocatedMemory(val fragments: List<Int>, val fragment: Fragment) : AutoCloseable {
    override fun close() {
        fragments.forEach {
            fragment.free(it)
        }
    }
}
