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
import kotlin.math.ceil

//complicated flow control....
data class MetaSegment(
    val commands: List<Command>,
    val loops: List<IntRange>,
    val branches: List<Pair<IntRange, IntRange?>>
) : Segment {
    companion object {
        fun buildControlGraph(instructions: List<Command>): MetaSegment {
            val loops = mutableListOf<IntRange>()
            val branches = mutableListOf<Pair<IntRange, IntRange?>>()

            instructions.forEachIndexed { index, command ->
                if (command is SimpleValue.JumpShort) {
                    if (command.offset < 0) { //loops must go backwards right?
                        loops.add((index + command.offset + 1)..index)
                    }
                }
                if (command is SimpleValue.IfTypeShort) {
                    if (command.offset() < 0) { //if loop... fun....
                        loops.add((index + command.offset() + 1)..index)
                    } else {
                        val ifBlock = index..(index + command.offset())
                        val last = instructions[ifBlock.last]
                        val elseBlock = if (last is SimpleValue.JumpShort && last.offset > 0) {
                            (ifBlock.last + 1)..(ifBlock.last + last.offset)
                        } else {
                            null
                        }
                        branches.add(Pair(ifBlock, elseBlock))
                    }
                }
            }
            return MetaSegment(instructions, loops, branches)
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
        TODO("Not yet implemented")
    }

    override fun size(): Int {
        return commands.size
    }

    override fun toBulk(storage: MutableList<Command>) {
        storage.addAll(commands)
    }

    override fun toBin(buffer: MemorySegmentBuffer) {
        buffer.writeVar(commands.size)
        commands.forEach {
            it.toBin(buffer)
        }
        buffer.writeVar(loops.size)
        loops.forEach {
            buffer.writeVar(it.first)
            buffer.writeVar(it.last)
        }
        buffer.writeVar(branches.size)
        branches.forEach {
            buffer.writeVar(it.first.first)
            buffer.writeVar(it.first.last)
            if (it.second == null) {
                buffer.write(0.toByte())
            } else {
                buffer.write(1.toByte())
                buffer.writeVar(it.second!!.first)
                buffer.writeVar(it.second!!.last)
            }
        }
    }

    override fun sizeBytes(): Long {
        return MemorySegmentBuffer.varSize(commands.size) + ceil(commands.sumOf { it.sizeBits() } / 8.0).toLong() + MemorySegmentBuffer.varSize(
            loops.size
        ) + loops.sumOf { MemorySegmentBuffer.varSize(it.first) + MemorySegmentBuffer.varSize(it.last) } + MemorySegmentBuffer.varSize(
            branches.size
        ) + branches.sumOf {
            MemorySegmentBuffer.varSize(it.first.first) + MemorySegmentBuffer.varSize(it.first.last) + 1 + if (it.second == null) {
                0
            } else {
                MemorySegmentBuffer.varSize(
                    it.second!!.first
                ) + MemorySegmentBuffer.varSize(it.second!!.last)
            }
        }
    }
}
