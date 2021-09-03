package tech.poder.ir.data.storage.memory

import java.io.Closeable

data class AllocatedMemory(val fragments: List<Int>, val fragment: Fragment) : Closeable {

    override fun close() {
        fragments.forEach(fragment::free)
    }

}
