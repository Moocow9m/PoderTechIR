package tech.poder.ir.data.storage.segment

import tech.poder.ir.data.base.Method
import tech.poder.ir.data.storage.Instruction
import tech.poder.ir.data.storage.Type
import tech.poder.ptir.data.Label
import java.util.*
import kotlin.reflect.KClass

data class MultiSegment(
    val instructions: MutableList<Segment> = mutableListOf()
) : Segment {

    /*companion object {

        fun buildSegments(raw: List<Instruction>?, startIndex: Int = 0): Segment? {

            if (raw == null) {
                return null
            }

            val loopIndexes = mutableMapOf<Int, Int>()

            raw.forEachIndexed { index, instruction ->

                if (instruction.opCode != Simple.JMP) {
                    return@forEachIndexed
                }

                val offset = (instruction.extra as Label).offset
                val instIndex = (offset - startIndex) + 1

                if (instIndex >= 0 && offset < index + startIndex) {
                    loopIndexes[index] = instIndex
                }
            }

            val head = MultiSegment()
            var tmpStorage = SegmentPart()

            var internalIndex = 0

            while (internalIndex < raw.size) {

                when (raw[internalIndex].opCode) {

                    Simple.IF_EQ, Simple.IF_LT_EQ,
                    Simple.IF_LT, Simple.IF_NOT_EQ,
                    Simple.IF_GT, Simple.IF_GT_EQ,
                    -> {

                        val potentialElse = raw[internalIndex].extra as Label

                        tmpStorage.instructions.add(raw[internalIndex])
                        head.instructions.add(tmpStorage)

                        val savedA = internalIndex

                        val ifRaw = ((internalIndex + 1)..potentialElse.offset).map {
                            raw[it]
                        }

                        internalIndex = potentialElse.offset

                        val savedB = internalIndex
                        val last = ifRaw.last()
                        var elseRaw: List<Instruction>? = null

                        if (last.opCode == Simple.JMP && (last.extra as Label).offset > (startIndex + internalIndex)) {

                            elseRaw = ((internalIndex + 1)..(last.extra as Label).offset).map {
                                raw[it]
                            }

                            internalIndex = (last.extra as Label).offset
                        }

                        head.instructions.add(
                            BranchHolder(
                                buildSegments(ifRaw, startIndex + savedA + 1)!!,
                                buildSegments(elseRaw, startIndex + savedB + 1)
                            )
                        )

                        tmpStorage = SegmentPart()
                    }
                    Simple.SWITCH -> {
                        TODO("SWITCH STATEMENTS NOT SUPPORTED YET!")
                    }
                    else -> {

                        if (internalIndex in loopIndexes.values) {

                            val jumpTo = loopIndexes.filter { it.value == internalIndex }.map { it.toPair() }

                            check(jumpTo.size == 1) {
                                "Loop detection failed! Jump point had multiple labels pointing to it: ${
                                    jumpTo.joinToString(
                                        ", "
                                    ) { "Jump from ${it.first} to ${it.second}" }
                                }"
                            }

                            val first = jumpTo.first()

                            if (tmpStorage.instructions.isNotEmpty()) {
                                head.instructions.add(tmpStorage)
                            }

                            val newRaw = (internalIndex..first.first).map { raw[it] }
                            head.instructions.add(LoopHolder(buildSegments(newRaw, startIndex + internalIndex + 1)!!))

                            if (tmpStorage.instructions.isNotEmpty()) {
                                tmpStorage = SegmentPart()
                            }

                            internalIndex = first.first
                        }
                        else {
                            tmpStorage.instructions.add(raw[internalIndex])
                        }
                    }
                }
                internalIndex++
            }

            if (tmpStorage.instructions.isNotEmpty()) {
                head.instructions.add(tmpStorage)
            }

            if (head.instructions.size == 1) {
                return head.instructions[0]
            }

            return head
        }
    }

    override fun eval(
        method: Method,
        stack: Stack<Type>,
        currentVars: MutableList<KClass<out Type>?>,
        currentIndex: Int,
        labels: MutableMap<Int, Label>
    ): Int {

        var index = currentIndex

        instructions.forEach {
            index = it.eval(method, stack, currentVars, index, labels)
        }

        return index
    }

    override fun size(): Int {
        return instructions.sumOf { it.size() }
    }

    override fun toBulk(storage: MutableList<Instruction>) {
        instructions.forEach {
            it.toBulk(storage)
        }
    }*/
    override fun eval(
        method: Method,
        stack: Stack<Type>,
        currentVars: MutableList<KClass<out Type>?>,
        currentIndex: Int,
        labels: MutableMap<Int, Label>
    ): Int {
        TODO("Not yet implemented")
    }

    override fun size(): Int {
        TODO("Not yet implemented")
    }

    override fun toBulk(storage: MutableList<Instruction>) {
        TODO("Not yet implemented")
    }
}
