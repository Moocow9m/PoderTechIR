package tech.poder.ir.data.storage.segment

import tech.poder.ir.commands.Command
import tech.poder.ir.commands.SimpleValue
import tech.poder.ir.data.Label
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.unlinked.UnlinkedMethod
import tech.poder.ir.util.MemorySegmentBuffer
import java.util.*

@JvmInline
value class MultiSegment(
    val instructions: MutableList<Segment> = mutableListOf()
) : Segment {

    companion object {

        fun buildSegments(raw: List<Command>, startIndex: Int = 0): Segment {

            val loopIndexes = mutableMapOf<Int, Int>()

            raw.forEachIndexed { index, instruction ->

                if (instruction !is SimpleValue.Jump) {
                    return@forEachIndexed
                }

                val offset = instruction.data.offset
                val instIndex = (offset - startIndex) + 1

                if (instIndex >= 0 && offset < index + startIndex) {
                    loopIndexes[index] = instIndex
                }
            }

            val head = MultiSegment()
            var tmpStorage = SegmentPart()

            var internalIndex = 0

            while (internalIndex < raw.size) {

                when (val instruction = raw[internalIndex]) {

                    is SimpleValue.IfType,
                    -> {
                        val potentialElse = instruction.label()

                        tmpStorage.instructions.add(raw[internalIndex])
                        head.instructions.add(tmpStorage)

                        val savedA = internalIndex

                        val ifRaw = ((internalIndex + 1)..potentialElse.offset).map {
                            raw[it]
                        }

                        internalIndex = potentialElse.offset

                        val savedB = internalIndex
                        val last = ifRaw.last()
                        var elseRaw: List<Command>? = null

                        if (last is SimpleValue.Jump && last.data.offset > (startIndex + internalIndex)) {

                            elseRaw = ((internalIndex + 1)..last.data.offset).map {
                                raw[it]
                            }

                            internalIndex = last.data.offset
                        }

                        head.instructions.add(
                            if (elseRaw == null) {
                                BranchHolder(
                                    buildSegments(ifRaw, startIndex + savedA + 1),
                                    null
                                )
                            } else {
                                BranchHolder(
                                    buildSegments(ifRaw, startIndex + savedA + 1),
                                    buildSegments(elseRaw, startIndex + savedB + 1)
                                )
                            }
                        )

                        tmpStorage = SegmentPart()
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
                            head.instructions.add(LoopHolder(buildSegments(newRaw, startIndex + internalIndex + 1)))

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

    /*override fun eval(
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



    override fun toBulk(storage: MutableList<Instruction>) {
        instructions.forEach {
            it.toBulk(storage)
        }
    }*/
    override fun eval(
        method: UnlinkedMethod,
        stack: Stack<Type>,
        currentVars: MutableList<Type>,
        currentIndex: Int,
        labels: MutableMap<Int, Label>
    ): Int {
        TODO("Not yet implemented")
    }

    override fun size(): Int {
        return instructions.sumOf { it.size() }
    }

    override fun toBulk(storage: List<Command>) {
        TODO("Not yet implemented")
    }

    override fun toBin(buffer: MemorySegmentBuffer) {
        buffer.write(0.toByte())
        buffer.writeVar(instructions.size)
        instructions.forEach {
            it.toBin(buffer)
        }
    }

    override fun sizeBytes(): Long {
        return 1L + MemorySegmentBuffer.varSize(instructions.size) + instructions.sumOf { it.sizeBytes() }
    }
}
