package tech.poder.ir.data.storage.segment

import tech.poder.ir.commands.Command
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Container
import tech.poder.ir.data.base.unlinked.UnlinkedMethod
import tech.poder.ir.util.MemorySegmentBuffer
import java.util.*

data class BranchHolder(
    val ifBlock: Segment,
    val elseBlock: Segment?
) : Segment {

    override fun eval(
        dependencies: Set<Container>,
        self: Container,
        method: UnlinkedMethod,
        stack: Stack<Type>,
        currentIndex: Int,
        vars: MutableMap<CharSequence, UInt>,
        type: MutableMap<UInt, Type>
    ): Int {

        val ifStack = Stack<Type>()
        val elseStack = Stack<Type>()

        stack.forEach {
            ifStack.push(it)
            elseStack.push(it)
        }

        stack.clear()

        val typeClone = type.map { it.key }

        var index = ifBlock.eval(dependencies, self, method, ifStack, currentIndex, vars, type)
        var removed = vars.filter { !typeClone.contains(it.value) } //remove scoped vars
        removed.forEach {
            vars.remove(it.key)
            type.remove(it.value)
        }
        index = elseBlock?.eval(dependencies, self, method, elseStack, index, vars, type) ?: index
        removed = vars.filter { !typeClone.contains(it.value) }
        removed.forEach {
            vars.remove(it.key)
            type.remove(it.value)
        }
        check(ifStack.size == elseStack.size) {
            "Branch stacks do not match!\n\tIf:\n\t\t${ifStack.joinToString("\n\t\t")}\n\tElse:\n\t\t${
                elseStack.joinToString("\n\t\t")
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
        return index
    }

    override fun size(): Int {
        return ifBlock.size() + (elseBlock?.size() ?: 0)
    }

    override fun toBulk(storage: MutableList<Command>) {
        ifBlock.toBulk(storage)
        elseBlock?.toBulk(storage)
    }

    override fun toBin(buffer: MemorySegmentBuffer) {
        buffer.write(3.toByte())
        ifBlock.toBin(buffer)
        if (elseBlock == null) {
            buffer.write(0.toByte())
        } else {
            buffer.write(1.toByte())
            elseBlock.toBin(buffer)
        }
    }

    override fun sizeBytes(): Long {
        return 2L + ifBlock.sizeBytes() + (elseBlock?.sizeBytes() ?: 0)
    }
}