package tech.poder.ir.data.storage.memory

import java.util.concurrent.locks.ReentrantLock
import kotlin.experimental.or
import kotlin.math.floor

data class Fragment(
    val flags: Byte,
    val position: Long,
    val size: Long,
    val objectSize: Int,
): Comparable<Fragment> {
    private var lastId: Int = 0
    private var count: Int = 0
    private val freeList: MutableList<Int> = mutableListOf()
    var state: State = State.EMPTY
    private val maxObject = floor(size / objectSize.toDouble()).toInt()
    private val writeLock = ReentrantLock()

    fun remaining(): Int {
        return freeList.size + (maxObject - count)
    }

    fun lock() {
        writeLock.lock()
    }

    fun unlock() {
        writeLock.unlock()
    }

    private fun updateState() {
        state = if (freeList.isEmpty() && lastId == 0) {
            State.EMPTY
        } else if (remaining() == 0) {
            State.FULL
        } else {
            State.PARTIAL
        }
    }

    fun nextFree(): Int {
        count++
        return if (freeList.isEmpty()) { //use fragmented areas first!
            objectSize * lastId++
        } else {
            freeList.removeAt(0)
        }.let {
            updateState()
            it
        }
    }

    fun free(location: Int) {
        count--
        if (lastId * objectSize == location) {
            lastId--
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

    override fun hashCode(): Int {
        return position.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Fragment) return false

        if (position != other.position) return false

        return true
    }

    override fun compareTo(other: Fragment): Int {
        return position.compareTo(other.position)
    }
}
