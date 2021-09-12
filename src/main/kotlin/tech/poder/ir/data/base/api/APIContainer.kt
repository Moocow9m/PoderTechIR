package tech.poder.ir.data.base.api

import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Container
import tech.poder.ir.data.base.Method
import tech.poder.ir.data.base.Object
import tech.poder.ir.metadata.NamedType
import tech.poder.ir.util.MemorySegmentBuffer

data class APIContainer(override val name: String, val entryPoint: UInt = 0u, val methods: List<PublicMethod>, val objects: List<PublicObject>, val fields: List<NamedType>): Container {
    override fun size(): Long {
        TODO("Not yet implemented")
    }

    override fun save(buffer: MemorySegmentBuffer) {
        TODO("Not yet implemented")
    }

    override fun locateField(name: String): UInt? {
        val loc = fields.indexOfFirst { it.name == name }
        if (loc == -1) {
            return null
        }
        return loc.toUInt()
    }

    override fun locateField(id: UInt): Type {
        return fields[id.toInt()].type
    }

    override fun locateMethod(name: String): UInt? {
        val loc = methods.indexOfFirst { it.name == name }
        if (loc == -1) {
            return null
        }
        return loc.toUInt()
    }

    override fun locateMethod(id: UInt): Method {
        return methods[id.toInt() - 1] //0 is reserved as null for methods
    }

    override fun locateObject(name: String): UInt? {
        val loc = objects.indexOfFirst { it.name == name }
        if (loc == -1) {
            return null
        }
        return loc.toUInt()
    }

    override fun locateObject(id: UInt): Object {
        return objects[id.toInt()]
    }

}