package tech.poder.ir

data class Task(val priority: UInt, val task: String) : Comparable<Task> {
    override fun compareTo(other: Task): Int {
        return priority.compareTo(other.priority)
    }
}