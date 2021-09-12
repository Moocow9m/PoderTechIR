package tech.poder.ir.data.base.api

import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Container
import tech.poder.ir.data.base.Method
import tech.poder.ir.util.MemorySegmentBuffer

data class APIContainer(
    override val name: String,
    val entryPoint: Int,
    val methods: List<PublicMethod>,
    val objects: List<PublicObject>
) : Container {
    override fun size(): Long {
        return 1L + MemorySegmentBuffer.sequenceSize(name) + MemorySegmentBuffer.varSize(entryPoint.toInt()) + MemorySegmentBuffer.varSize(
            methods.size
        ) + methods.sumOf { it.size() } + MemorySegmentBuffer.varSize(objects.size) + objects.sumOf { it.size() }
    }

    override fun save(buffer: MemorySegmentBuffer) {
        buffer.write(1.toByte())
        buffer.writeSequence(name)
        buffer.writeVar(entryPoint.toInt())
        buffer.writeVar(methods.size)
        methods.forEach {
            it.save(buffer)
        }
        buffer.writeVar(objects.size)
        objects.forEach {
            it.save(buffer)
        }
    }

    override fun locateField(obj: UInt, name: String): UInt? {
        val loc = locateObject(obj).fields.indexOfFirst { it.name == name }
        if (loc == -1) {
            return null
        }
        return loc.toUInt()
    }

    override fun locateField(obj: UInt, id: UInt): Type {
        return locateObject(obj).fields[id.toInt()].type
    }

    override fun locateMethod(name: String): UInt? {
        val loc = methods.indexOfFirst { it.name == name }
        if (loc == -1) {
            return null
        }
        return loc.toUInt()
    }

    override fun locateMethod(id: UInt): Method {
        return methods[id.toInt()]
    }

    override fun locateObject(name: String): UInt? {
        val loc = objects.indexOfFirst { it.name == name }
        if (loc == -1) {
            return null
        }
        return loc.toUInt()
    }

    override fun locateObject(id: UInt): PublicObject {
        return objects[id.toInt()]
    }

}