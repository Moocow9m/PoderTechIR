package tech.poder.ir.data.storage.segment

import tech.poder.ir.data.storage.Instruction
import tech.poder.ptir.data.Label
import tech.poder.ptir.data.Type
import tech.poder.ptir.data.base.Method
import java.util.*
import kotlin.reflect.KClass

data class LoopHolder(val block: Segment) : Segment {

    override fun eval(
        method: Method,
        stack: Stack<Type>,
        currentVars: MutableList<KClass<out Type>?>,
        currentIndex: Int,
        labels: MutableMap<Int, Label>
    ): Int {

        val loopStack = Stack<Type>()
        val copy = Stack<Type>()

        stack.forEach {
            loopStack.push(it)
        }

        val index = block.eval(method, stack, currentVars, currentIndex, labels)

        check(loopStack.size == copy.size) {
            "Loop stack does not match original!\n\tLoop:\n\t\t${loopStack.joinToString("\n\t\t")}\n\tOriginal:\n\t\t${
                copy.joinToString("\n\t\t")
            }"
        }

        repeat(loopStack.size) {

            val popA = loopStack.pop()
            val popB = copy.pop()

            check(popA == popB) {
                "Loop stack does not match original! Loop: $popA Original: $popB\n\tLoop:\n\t\t${
                    loopStack.joinToString("\n\t\t")
                }\n\tOriginal:\n\t\t${
                    copy.joinToString("\n\t\t")
                }"
            }
        }
        return index
    }

    override fun size(): Int {
        return block.size()
    }

    override fun toBulk(storage: MutableList<Instruction>) {
        block.toBulk(storage)
    }
}