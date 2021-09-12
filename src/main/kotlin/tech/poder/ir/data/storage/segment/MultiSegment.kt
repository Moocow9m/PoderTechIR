package tech.poder.ir.data.storage.segment

import tech.poder.ir.commands.Command
import tech.poder.ir.commands.SimpleValue
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.api.APIContainer
import tech.poder.ir.data.base.unlinked.UnlinkedContainer
import tech.poder.ir.data.base.unlinked.UnlinkedMethod
import tech.poder.ir.metadata.NameId
import tech.poder.ir.util.MemorySegmentBuffer
import java.util.*

@JvmInline
value class MultiSegment(
    val instructions: MutableList<Segment> = mutableListOf()
) : Segment {

    companion object {

        fun buildSegments(raw: List<Command>, startIndex: Int = 0, isLoop: Boolean = false): Segment {

            val loopIndexes = mutableMapOf<Int, Int>()

            raw.forEachIndexed { index, instruction ->

                if (instruction !is SimpleValue.JumpShort) {
                    return@forEachIndexed
                }

                val offset = (index + instruction.offset) + 1
                if (isLoop) {
                    if (offset > 0 && instruction.offset < 0) {
                        loopIndexes[offset] = index
                    }
                } else {
                    if (offset > -1 && instruction.offset < 0) {
                        loopIndexes[offset] = index
                    }
                }

            }

            val head = MultiSegment()
            var tmpStorage = SegmentPart()

            var internalIndex = 0

            while (internalIndex < raw.size) {

                when (val instruction = raw[internalIndex]) {
                    is SimpleValue.IfTypeShort -> {
                        val potentialElse = instruction.offset()

                        tmpStorage.instructions.add(raw[internalIndex])
                        head.instructions.add(tmpStorage)

                        val savedA = internalIndex

                        val ifRaw = ((internalIndex + 1)..(internalIndex + potentialElse)).map {
                            raw[it]
                        }

                        internalIndex += potentialElse

                        val savedB = internalIndex
                        val last = ifRaw.last()
                        var elseRaw: List<Command>? = null

                        if (last is SimpleValue.JumpShort && last.offset > 0) {

                            elseRaw = ((internalIndex + 1)..(internalIndex + last.offset)).map {
                                raw[it]
                            }

                            internalIndex += last.offset
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
                        if (loopIndexes.containsKey(internalIndex)) {

                            val jumpTo = loopIndexes[internalIndex]!!

                            if (tmpStorage.instructions.isNotEmpty()) {
                                head.instructions.add(tmpStorage)
                            }

                            val newRaw = (internalIndex..jumpTo).map { raw[it] }
                            head.instructions.add(
                                LoopHolder(
                                    buildSegments(
                                        newRaw,
                                        startIndex + internalIndex + 1,
                                        true
                                    )
                                )
                            )

                            if (tmpStorage.instructions.isNotEmpty()) {
                                tmpStorage = SegmentPart()
                            }

                            internalIndex = jumpTo
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
        dependencies: Set<APIContainer>,
        self: UnlinkedContainer,
        method: UnlinkedMethod,
        stack: Stack<Type>,
        currentIndex: Int,
        vars: MutableMap<CharSequence, UInt>,
        type: MutableMap<UInt, Type>,
        depMap: List<NameId>
    ): Int {
        var index = currentIndex

        instructions.forEach {
            index = it.eval(dependencies, self, method, stack, index, vars, type, depMap)
        }

        return index
    }

    override fun size(): Int {
        return instructions.sumOf { it.size() }
    }

    override fun toBulk(storage: MutableList<Command>) {
        instructions.forEach {
            it.toBulk(storage)
        }
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
