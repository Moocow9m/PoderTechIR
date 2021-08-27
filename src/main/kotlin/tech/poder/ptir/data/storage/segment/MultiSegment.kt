package tech.poder.ptir.data.storage.segment

import tech.poder.ptir.commands.Simple
import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Label
import tech.poder.ptir.data.storage.Type

data class MultiSegment(
    val instructions: ArrayList<Segment> = arrayListOf(),
    val stackChanges: ArrayList<Type> = arrayListOf()
) : Segment {
    companion object {
        fun buildSegments(raw: ArrayList<Instruction>): MultiSegment {
            val head = MultiSegment()
            val loopIndexes = mutableMapOf<Int, Int>()
            raw.forEachIndexed { index, instruction ->
                if (instruction.opCode == Simple.JMP) {
                    val offset = (instruction.extra as Label).offset
                    if (offset < index) {
                        loopIndexes[index] = offset
                    }
                }
            }
            var index = 0
            var tmpStorage = SegmentPart()
            while (index < raw.size) {
                if (loopIndexes.values.contains(index)) {
                    val jumpTo = loopIndexes.filter { it.value == index }.map { it.toPair() }
                    check(jumpTo.size == 1) {
                        "Loop detection failed! Jump point had multiple labels pointing to it: ${jumpTo.joinToString(", ") { "Jump from ${it.first} to ${it.second}" }}"
                    }
                    val first = jumpTo.first()

                    if (tmpStorage.data.isNotEmpty()) {
                        head.instructions.add(tmpStorage)
                    }

                    val newRaw = ArrayList<Instruction>()
                    (index..first.first).forEach { newRaw.add(raw[it]) }
                    head.instructions.add(buildSegments(newRaw))

                    if (tmpStorage.data.isNotEmpty()) {
                        tmpStorage = SegmentPart()
                    }

                    index = first.first
                } else {
                    when (raw[index].opCode) {
                        Simple.IF_EQ, Simple.IF_LT_EQ,
                        Simple.IF_LT, Simple.IF_NOT_EQ,
                        Simple.IF_GT, Simple.IF_GT_EQ -> {
                            val label = raw[index].extra as Label

                            if (tmpStorage.data.isNotEmpty()) {
                                head.instructions.add(tmpStorage)
                            }

                            //todo package if

                            if (tmpStorage.data.isNotEmpty()) {
                                tmpStorage = SegmentPart()
                            }
                            //todo move index to after if(and else if applicable)
                        }
                        Simple.SWITCH -> {
                            TODO("SWITCH STATEMENTS NOT SUPPORTED YET!")
                        }
                        else -> tmpStorage.data.add(raw[index])
                    }
                }
                index++
            }
            if (tmpStorage.data.isNotEmpty()) {
                head.instructions.add(tmpStorage)
            }
            return head
        }
    }

    fun eval() {

    }
}
