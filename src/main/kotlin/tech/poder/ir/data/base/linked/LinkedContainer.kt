package tech.poder.ir.data.base.linked

import tech.poder.ir.commands.Command
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Container
import tech.poder.ir.data.base.Method
import tech.poder.ir.data.base.Object
import tech.poder.ir.util.MemorySegmentBuffer
import kotlin.math.ceil

data class LinkedContainer(
    override val name: String,
    val entryPoint: UInt,
    val depTable: List<CharSequence>,
    val methods: List<List<Command>>,
    val objects: List<List<Type>>,
) : Container {

    override fun size(): Long {
        return 1L + MemorySegmentBuffer.sequenceSize(name) + MemorySegmentBuffer.varSize(entryPoint.toInt()) + MemorySegmentBuffer.varSize(
            depTable.size
        ) + depTable.sumOf {
            MemorySegmentBuffer.sequenceSize(it).toLong()
        } + MemorySegmentBuffer.varSize(objects.size) + objects.sumOf { list ->
            MemorySegmentBuffer.varSize(list.size) + list.sumOf {
                it.size().toLong()
            }
        } + MemorySegmentBuffer.varSize(methods.size) + methods.sumOf { list ->
            MemorySegmentBuffer.varSize(list.size) +
                    list.sumOf { ceil(it.sizeBits() / 8.0).toLong() }
        }
    }

    override fun save(buffer: MemorySegmentBuffer) {
        buffer.write(2.toByte())
        buffer.writeSequence(name)
        buffer.writeVar(entryPoint.toInt())
        buffer.writeVar(depTable.size)
        depTable.forEach {
            buffer.writeSequence(it)
        }
        buffer.writeVar(objects.size)
        objects.forEach { list ->
            buffer.writeVar(list.size)
            list.forEach {
                it.toBin(buffer)
            }
        }
        buffer.writeVar(methods.size)
        methods.forEach { list ->
            buffer.writeVar(list.size)
            list.forEach {
                it.toBin(buffer)
            }
        }
    }

    override fun locateField(obj: UInt, name: String): UInt? {
        TODO("Not yet implemented")
    }

    override fun locateField(obj: UInt, id: UInt): Type {
        TODO("Not yet implemented")
    }

    override fun locateMethod(name: String): UInt? {
        TODO("Not yet implemented")
    }

    override fun locateMethod(id: UInt): Method {
        TODO("Not yet implemented")
    }

    override fun locateObject(name: String): UInt? {
        TODO("Not yet implemented")
    }

    override fun locateObject(id: UInt): Object {
        TODO("Not yet implemented")
    }
}
