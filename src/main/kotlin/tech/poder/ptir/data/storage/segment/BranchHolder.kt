package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.data.base.Method
import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Type
import java.util.*

data class BranchHolder(
    val ifBlock: Segment,
    val elseBlock: Segment?
) : Segment {
    override fun eval(method: Method, stack: Stack<Type>, currentVars: Array<Type?>) {
        val ifStack = Stack<Type>()
        val elseStack = Stack<Type>()
        stack.forEach {
            ifStack.push(it.copy())
            elseStack.push(it.copy())
        }
        stack.clear()
        ifBlock.eval(method, ifStack, currentVars) //todo verify same after running
        elseBlock?.eval(method, stack, currentVars) //todo verify same after running
        check(ifStack.size == elseStack.size) {
            "Branch stacks do not match!\n\tIf:\n\t\t${ifStack.joinToString("\n\t\t")}\n\tElse:\n\t\t${
                elseStack.joinToString(
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
    }

    override fun size(): Int {
        return ifBlock.size() + (elseBlock?.size() ?: 0)
    }

    override fun toBulk(storage: ArrayList<Instruction>) {
        ifBlock.toBulk(storage)
        elseBlock?.toBulk(storage)
    }
}