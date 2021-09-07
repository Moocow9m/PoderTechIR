package tech.poder.ir.data.storage.segment

import tech.poder.ir.data.base.Method
import tech.poder.ir.data.storage.Instruction
import tech.poder.ir.data.storage.Type
import tech.poder.ptir.data.Label
import java.util.*
import kotlin.reflect.KClass

data class BranchHolder(
    val ifBlock: Segment,
    val elseBlock: Segment?
) : Segment {

    override fun eval(
        method: Method,
        stack: Stack<Type>,
        currentVars: MutableList<KClass<out Type>?>,
        currentIndex: Int,
        labels: MutableMap<Int, Label>
    ): Int {

        val ifStack = Stack<Type>()
        val elseStack = Stack<Type>()

        stack.forEach {
            ifStack.push(it)
            elseStack.push(it)
        }

        val ifVars = MutableList(currentVars.size) {
            currentVars[it]
        }
        val elseVars = MutableList(currentVars.size) {
            currentVars[it]
        }

        stack.clear()
        var index = currentIndex
        index = ifBlock.eval(method, ifStack, ifVars, index, labels)
        index = elseBlock?.eval(method, stack, elseVars, index, labels) ?: index

        check(ifStack.size == elseStack.size) {
            "Branch stacks do not match!\n\tIf:\n\t\t${ifStack.joinToString("\n\t\t")}\n\tElse:\n\t\t${
                elseStack.joinToString("\n\t\t")
            }"
        }

        check(ifVars.size == elseVars.size) {
            "Branch vars do not match!\n\tIf:\n\t\t${ifVars.joinToString("\n\t\t")}\n\tElse:\n\t\t${
                elseVars.joinToString("\n\t\t")
            }"
        }

        ifStack.forEach {
            stack.push(it)
        }

        repeat(ifStack.size) {

            val popA = ifStack.pop()
            val popB = elseStack.pop()

            check(popA == popB) {
                "Branch stacks do not match! If: $popA Else: $popB\n\tIf:\n\t\t${ifStack.joinToString("\n\t\t")}\n\tElse:\n\t\t${
                    elseStack.joinToString("\n\t\t")
                }"
            }
        }
        repeat(ifVars.size) {

            val a = ifVars[it]
            val b = elseVars[it]

            check(a == b) {
                "Branch vars do not match! If: $a Else: $b\n\tIf:\n\t\t${ifVars.joinToString("\n\t\t")}\n\tElse:\n\t\t${
                    elseVars.joinToString("\n\t\t")
                }"
            }

            currentVars[it] = a
        }
        return index
    }

    override fun size(): Int {
        return ifBlock.size() + (elseBlock?.size() ?: 0)
    }

    override fun toBulk(storage: MutableList<Instruction>) {
        ifBlock.toBulk(storage)
        elseBlock?.toBulk(storage)
    }
}