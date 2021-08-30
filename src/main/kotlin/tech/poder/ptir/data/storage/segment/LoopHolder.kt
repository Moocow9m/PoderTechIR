package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.base.Method
import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Type
import java.util.*

data class LoopHolder(val block: Segment) : Segment {
    override fun eval(method: Method, stack: Stack<Type>, currentVars: Array<Type?>) {
        val loopStack = Stack<Type>()
        val copy = Stack<Type>()
        stack.forEach {
            loopStack.push(it.copy())
            copy.push(it.copy())
        }
        block.eval(method, stack, currentVars) //todo verify same after running
        check(loopStack.size == copy.size) {
            "Loop stack does not match original!\n\tLoop:\n\t\t${loopStack.joinToString("\n\t\t")}\n\tOriginal:\n\t\t${
                copy.joinToString(
                    "\n\t\t"
                )
            }"
        }
        repeat(loopStack.size) {
            val popA = loopStack.pop()
            val popB = copy.pop()
            if (popA is Type.Constant) {
                popA.constant = false
            }
            if (popB is Type.Constant) {
                popB.constant = false
            }
            check(popA == popB) {
                "Loop stack does not match original! Loop: $popA Original: $popB\n\tLoop:\n\t\t${
                    loopStack.joinToString(
                        "\n\t\t"
                    )
                }\n\tOriginal:\n\t\t${
                    copy.joinToString(
                        "\n\t\t"
                    )
                }"
            }
        }
    }

    override fun size(): Int {
        return block.size()
    }

    override fun toBulk(storage: ArrayList<Instruction>) {
        block.toBulk(storage)
    }
}