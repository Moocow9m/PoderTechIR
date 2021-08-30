package tech.poder.ir.data.storage.segment

import tech.poder.ir.data.base.Method
import tech.poder.ir.data.storage.Instruction
import tech.poder.ir.data.storage.Label
import tech.poder.ir.data.storage.Type
import java.util.*

data class BranchHolder(
    val ifBlock: Segment,
    val elseBlock: Segment?
) : Segment {
    override fun eval(
        method: Method,
        stack: Stack<Type>,
        currentVars: Array<Type?>,
        currentIndex: Int,
        labels: MutableMap<Int, Label>
    ): Int {
        val ifStack = Stack<Type>()
        val elseStack = Stack<Type>()
        stack.forEach {
            ifStack.push(it.copy())
            elseStack.push(it.copy())
        }
        val ifVars = Array(currentVars.size) {
            currentVars[it]?.copy()
        }
        val elseVars = Array(currentVars.size) {
            currentVars[it]?.copy()
        }
        stack.clear()
        var index = currentIndex
        index = ifBlock.eval(method, ifStack, ifVars, index, labels)
        index = elseBlock?.eval(method, stack, elseVars, index, labels) ?: index
        check(ifStack.size == elseStack.size) {
            "Branch stacks do not match!\n\tIf:\n\t\t${ifStack.joinToString("\n\t\t")}\n\tElse:\n\t\t${
                elseStack.joinToString(
                    "\n\t\t"
                )
            }"
        }
        check(ifVars.size == elseVars.size) {
            "Branch vars do not match!\n\tIf:\n\t\t${ifVars.joinToString("\n\t\t")}\n\tElse:\n\t\t${
                elseVars.joinToString(
                    "\n\t\t"
                )
            }"
        }
        ifStack.forEach {
            stack.push(it)
        }
        repeat(ifStack.size) {
            val popA = ifStack.pop()
            val popB = elseStack.pop()
            if (popA is Type.Constant) {
                popA.constant = false
            }
            if (popB is Type.Constant) {
                popB.constant = false
            }
            check(popA == popB) {
                "Branch stacks do not match! If: $popA Else: $popB\n\tIf:\n\t\t${ifStack.joinToString("\n\t\t")}\n\tElse:\n\t\t${
                    elseStack.joinToString(
                        "\n\t\t"
                    )
                }"
            }
        }
        repeat(ifVars.size) {
            val a = ifVars[it]
            val b = elseVars[it]
            if (a is Type.Constant) {
                a.constant = false
            }
            if (b is Type.Constant) {
                b.constant = false
            }
            check(a == b) {
                "Branch vars do not match! If: $a Else: $b\n\tIf:\n\t\t${ifVars.joinToString("\n\t\t")}\n\tElse:\n\t\t${
                    elseVars.joinToString(
                        "\n\t\t"
                    )
                }"
            }
            currentVars[it] = a
        }
        return index
    }

    override fun size(): Int {
        return ifBlock.size() + (elseBlock?.size() ?: 0)
    }

    override fun toBulk(storage: ArrayList<Instruction>) {
        ifBlock.toBulk(storage)
        elseBlock?.toBulk(storage)
    }
}