package tech.poder.ir.data.storage.memory

import kotlin.experimental.or

data class Fragment(
    val flags: Byte,
    val position: Long,
    val size: Int,
    val objectSize: Int,
    val freeList: MutableList<Int>
) {
    val isReadOnly by lazy {
        flags or 1
    }

    val isExecutable by lazy {
        flags or 2
    }

    val reservedA by lazy {
        flags or 4
    }

    val reservedB by lazy {
        flags or 8
    }

    val reservedC by lazy {
        flags or 16
    }

    val reservedD by lazy {
        flags or 32
    }

    val reservedE by lazy {
        flags or 64
    }

    val reservedF by lazy {
        flags or 128.toByte()
    }
}
