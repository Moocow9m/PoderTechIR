package tech.poder.ir.data.storage.memory

data class AllocatedMemory(val start: Long, val fragments: List<Int>, val fragmentSize: Int)
