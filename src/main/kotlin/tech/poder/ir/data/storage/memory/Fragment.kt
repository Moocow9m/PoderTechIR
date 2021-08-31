package tech.poder.ir.data.storage.memory

import kotlin.experimental.or

data class Fragment(
    val flags: Byte,
    val position: Long,
    val size: Int,
    val objectSize: Int,
    var count: Int = 0,
    val freeList: MutableList<Int> = mutableListOf(),
    var state: State = State.EMPTY
) {

    private fun updateState() {
        state = if (freeList.isEmpty() && count == 0) {
            State.EMPTY
        } else if (freeList.isEmpty() && size == (count * objectSize)) {
            State.FULL
        } else {
            State.PARTIAL
        }
    }

    fun nextFree(): Int {
        return if (freeList.isEmpty()) { //use fragmented areas first!
            objectSize * count++
        } else {
            freeList.removeAt(0)
        }.let {
            updateState()
            it
        }
    }

    fun free(location: Int) {
        if (count * objectSize == location) {
            count--
        } else {
            freeList.add(location)
        }
        updateState()
    }

    val isReadOnly by lazy {
        flags or 1 != 0.toByte()
    }

    val isExecutable by lazy {
        flags or 2 != 0.toByte()
    }

    val reservedA by lazy {
        flags or 4 != 0.toByte()
    }

    val reservedB by lazy {
        flags or 8 != 0.toByte()
    }

    val reservedC by lazy {
        flags or 16 != 0.toByte()
    }

    val reservedD by lazy {
        flags or 32 != 0.toByte()
    }

    val reservedE by lazy {
        flags or 64 != 0.toByte()
    }

    val reservedF by lazy {
        flags or 128.toByte() != 0.toByte()
    }
}
