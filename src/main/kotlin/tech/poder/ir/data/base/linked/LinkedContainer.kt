package tech.poder.ir.data.base.linked

import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Container
import tech.poder.ir.data.base.Method
import tech.poder.ir.data.base.Object
import tech.poder.ir.data.base.unlinked.UnlinkedObject
import tech.poder.ir.metadata.NameId
import tech.poder.ir.util.MemorySegmentBuffer

data class LinkedContainer(
    override val name: String,
    val entryPoint: UInt = 0u,
    val depTable: List<NameId>,
    val packages: List<LinkedPackage>
) : Container {
    private var internalMappingCache: Map<String, UInt>? = null

    internal fun getSelfMapping(): Map<String, UInt> {
        if (internalMappingCache != null) {
            return internalMappingCache!!
        }
        val map = mutableMapOf<String, UInt>()
        packages.forEach { pkg ->
            pkg.methods.filterIsInstance<PublicMethod>().forEach {
                map[it.name] = it.id
            }
            pkg.objects.filterIsInstance<PublicObject>().forEach { obj ->
                map[obj.name] = obj.id
                obj.methods.filterIsInstance<PublicMethod>().forEach {
                    map[it.name] = it.id
                }
                obj.fields.forEach {
                    map["${obj.name}${UnlinkedObject.fieldSeparator}${it.name}"] = it.id
                }
            }
        }
        internalMappingCache = map
        return map
    }

    override fun size(): Long {
        return 1 + MemorySegmentBuffer.sequenceSize(name) + MemorySegmentBuffer.varSize(entryPoint.toInt()) + MemorySegmentBuffer.varSize(
            depTable.size
        ) + depTable.sumOf { MemorySegmentBuffer.sequenceSize(it.name) + MemorySegmentBuffer.varSize(it.id.toInt()) } + MemorySegmentBuffer.varSize(
            packages.size
        ) + packages.sumOf { it.size() }
    }

    override fun save(buffer: MemorySegmentBuffer) {
        buffer.write(1.toByte())
        buffer.writeSequence(name)
        buffer.writeVar(entryPoint.toInt())
        buffer.writeVar(depTable.size)
        depTable.forEach {
            buffer.writeSequence(it.name)
            buffer.writeVar(it.id.toInt())
        }
        buffer.writeVar(packages.size)
        packages.forEach {
            it.save(buffer)
        }
    }

    override fun locateField(name: String): UInt? {
        return getSelfMapping()[name]
    }

    override fun locateField(id: UInt): Type {
        var type: Type = Type.Unit
        packages.first { linkedPackage ->
            linkedPackage.objects.firstOrNull { obj ->
                if (obj is PublicObject) {
                    obj.fields.firstOrNull {
                        if (it.id == id) {
                            type = it.type
                            true
                        } else {
                            false
                        }
                    } != null
                } else {
                    obj as PrivateObject
                    obj.fields.firstOrNull {
                        if (it.id == id) {
                            type = it.type
                            true
                        } else {
                            false
                        }
                    } != null
                }
            } != null
        }
        return type
    }

    override fun locateMethod(name: String): UInt? {
        return getSelfMapping()[name]
    }

    override fun locateMethod(id: UInt): Method {
        var method: Method? = null
        packages.first { linkedPackage ->
            linkedPackage.objects.firstOrNull { obj ->
                if (obj is PublicObject) {
                    obj.methods.firstOrNull {
                        if (isMethod(it, id)) {
                            method = it
                            true
                        } else {
                            false
                        }
                    } != null
                } else {
                    obj as PrivateObject
                    obj.methods.firstOrNull {
                        if (it.id == id) {
                            method = it
                            true
                        } else {
                            false
                        }
                    } != null
                }
            } != null || linkedPackage.methods.firstOrNull {
                if (isMethod(it, id)) {
                    method = it
                    true
                } else {
                    false
                }
            } != null
        }
        return method!!
    }

    private fun isMethod(it: Method, id: UInt): Boolean {
        return if (it is PublicMethod) {
            it.id == id
        } else {
            it as PrivateMethod
            it.id == id
        }
    }

    override fun locateObject(name: String): UInt? {
        return getSelfMapping()[name]
    }

    override fun locateObject(id: UInt): Object {
        var obj2: Object? = null
        packages.first { linkedPackage ->
            linkedPackage.objects.firstOrNull { obj ->
                if (obj is PublicObject) {
                    if (obj.id == id) {
                        obj2 = obj
                        true
                    } else {
                        false
                    }
                } else {
                    obj as PrivateObject
                    obj.fields.firstOrNull {
                        if (obj.id == id) {
                            obj2 = obj
                            true
                        } else {
                            false
                        }
                    } != null
                }
            } != null
        }
        return obj2!!
    }
}
