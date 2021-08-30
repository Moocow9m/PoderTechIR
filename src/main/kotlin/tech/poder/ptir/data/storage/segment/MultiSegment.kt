package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.commands.Simple
import tech.poder.ptir.data.base.Method
import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Label
import tech.poder.ptir.data.storage.Type
import java.util.*

data class MultiSegment(
    val instructions: ArrayList<Segment> = arrayListOf()
) : Segment {
    companion object {
        fun buildSegments(raw: ArrayList<Instruction>?, startIndex: Int = 0): Segment? {
            if (raw == null) {
                return null
            }

            val loopIndexes = mutableMapOf<Int, Int>()
            raw.forEachIndexed { index, instruction ->
                if (instruction.opCode == Simple.JMP) {
                    val offset = (instruction.extra as Label).offset
                    val instIndex = (offset - startIndex) + 1
                    if (instIndex >= 0) {
                        if (offset < index + startIndex) {
                            loopIndexes[index] = instIndex
                        }
                    }

                }
            }
            val head = MultiSegment()
            var internalIndex = 0
            var tmpStorage = SegmentPart()
            while (internalIndex < raw.size) {
                when (raw[internalIndex].opCode) {
                    Simple.IF_EQ, Simple.IF_LT_EQ,
                    Simple.IF_LT, Simple.IF_NOT_EQ,
                    Simple.IF_GT, Simple.IF_GT_EQ -> {
                        val potentialElse = raw[internalIndex].extra as Label
                        tmpStorage.data.add(raw[internalIndex])
                        head.instructions.add(tmpStorage)

                        val savedA = internalIndex
                        val ifRaw = ArrayList<Instruction>()
                        ((internalIndex + 1)..potentialElse.offset).forEach { ifRaw.add(raw[it]) }
                        internalIndex = potentialElse.offset
                        val savedB = internalIndex
                        val last = ifRaw.last()
                        var elseRaw: ArrayList<Instruction>? = null
                        if (last.opCode == Simple.JMP && (last.extra as Label).offset > (startIndex + internalIndex)) {
                            elseRaw = arrayListOf()
                            ((internalIndex + 1)..(last.extra as Label).offset).forEach { elseRaw.add(raw[it]) }
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
                        if (loopIndexes.values.contains(internalIndex)) {
                            val jumpTo = loopIndexes.filter { it.value == internalIndex }.map { it.toPair() }
                            check(jumpTo.size == 1) {
                                "Loop detection failed! Jump point had multiple labels pointing to it: ${
                                    jumpTo.joinToString(
                                        ", "
                                    ) { "Jump from ${it.first} to ${it.second}" }
                                }"
                            }
                            val first = jumpTo.first()

                            if (tmpStorage.data.isNotEmpty()) {
                                head.instructions.add(tmpStorage)
                            }

                            val newRaw = ArrayList<Instruction>()
                            (internalIndex..first.first).forEach { newRaw.add(raw[it]) }
                            head.instructions.add(LoopHolder(buildSegments(newRaw, startIndex + internalIndex + 1)!!))

                            if (tmpStorage.data.isNotEmpty()) {
                                tmpStorage = SegmentPart()
                            }

                            internalIndex = first.first
                        } else {
                            tmpStorage.data.add(raw[internalIndex])
                        }
                    }
                }
                internalIndex++
            }
            if (tmpStorage.data.isNotEmpty()) {
                head.instructions.add(tmpStorage)
            }
            if (head.instructions.size == 1) {
                return head.instructions[0]
            }
            return head
        }
    }

    override fun eval(method: Method, stack: Stack<Type>, currentVars: Array<Type?>) {
        instructions.forEach {
            it.eval(method, stack, currentVars)
        }
    }

    override fun size(): Int {
        return instructions.sumOf { it.size() }
    }

    override fun toBulk(storage: ArrayList<Instruction>) {
        instructions.forEach {
            it.toBulk(storage)
        }
    }
}
