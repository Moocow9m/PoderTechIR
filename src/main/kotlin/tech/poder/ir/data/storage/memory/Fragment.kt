package tech.poder.ir.data.storage.memory

data class Fragment(val position: Long, val size: Int, val objectSize: Int, val freeList: MutableList<Int>)
