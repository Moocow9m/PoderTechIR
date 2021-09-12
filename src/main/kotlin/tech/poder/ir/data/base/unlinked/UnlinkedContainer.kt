package tech.poder.ir.data.base.unlinked

import tech.poder.ir.api.Optimizer
import tech.poder.ir.commands.Command
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Container
import tech.poder.ir.data.base.Method
import tech.poder.ir.data.base.Object
import tech.poder.ir.data.base.api.PublicMethod
import tech.poder.ir.data.base.api.PublicObject
import tech.poder.ir.data.base.linked.*
import tech.poder.ir.metadata.IdType
import tech.poder.ir.metadata.NameId
import tech.poder.ir.metadata.NamedIdType
import tech.poder.ir.metadata.Visibility
import tech.poder.ir.util.MemorySegmentBuffer
import java.util.*

class UnlinkedContainer(override val name: String) : Container {
    private val roots: MutableSet<UnlinkedPackage> = mutableSetOf()
    private var entryPoint: String = ""
    private var mappingCache: Map<String, UInt>? = null
    private var internalMappingCache: Map<String, UInt>? = null
    private var internalMappingCacheField: Map<UInt, Type>? = null
    private var internalMappingCacheMethod: Map<UInt, UnlinkedMethod>? = null
    private var internalMappingCacheObject: Map<UInt, UnlinkedObject>? = null

    private fun getFieldMapping(): Map<UInt, Type> {
        if (internalMappingCacheField == null) {
            getSelfMapping()
        }
        return internalMappingCacheField!!
    }

    private fun getMethodMapping(): Map<UInt, UnlinkedMethod> {
        if (internalMappingCacheMethod == null) {
            getSelfMapping()
        }
        return internalMappingCacheMethod!!
    }

    private fun getObjectMapping(): Map<UInt, UnlinkedObject> {
        if (internalMappingCacheObject == null) {
            getSelfMapping()
        }
        return internalMappingCacheObject!!
    }

    internal fun getSelfMapping(): Map<String, UInt> { //private items will have their name-dropped from binary on link!
        if (internalMappingCache != null) {
            return internalMappingCache!!
        }
        var nextMethodId = 1u //start at 1 so 0 can be null for entryPoint
        var nextObjectId = 0u
        var nextFieldId = 0u
        val map = mutableMapOf<String, UInt>()
        val map1 = mutableMapOf<UInt, Type>()
        val map2 = mutableMapOf<UInt, UnlinkedMethod>()
        val map3 = mutableMapOf<UInt, UnlinkedObject>()
        roots.filter { it.visibility == Visibility.PUBLIC }.forEach { pkg ->
            pkg.floating.filter { it.visibility == Visibility.PUBLIC }.forEach {
                val id = nextMethodId++
                map[it.fullName] = id
                map2[id] = it
            }
            pkg.objects.filter { it.visibility == Visibility.PUBLIC }.forEach { obj ->
                val id = nextObjectId++
                map[obj.fullName] = id
                map3[id] = obj
                obj.methods.filter { it.visibility == Visibility.PUBLIC }.forEach {
                    val id2 = nextMethodId++
                    map[it.fullName] = id2
                    map2[id2] = it
                }
                obj.fields.forEach {
                    val id2 = nextFieldId++
                    map["${obj.fullName}${UnlinkedObject.fieldSeparator}${it.name}"] = id2
                    map1[id2] = it.type
                }
            }
        }
        roots.filter { it.visibility == Visibility.PRIVATE }.forEach { pkg ->
            pkg.floating.filter { it.visibility == Visibility.PRIVATE }.forEach {
                val id = nextMethodId++
                map[it.fullName] = id
                map2[id] = it
            }
            pkg.objects.filter { it.visibility == Visibility.PRIVATE }.forEach { obj ->
                val id = nextObjectId++
                map[obj.fullName] = id
                map3[id] = obj
                obj.methods.filter { it.visibility == Visibility.PRIVATE }.forEach {
                    val id2 = nextMethodId++
                    map[it.fullName] = id2
                    map2[id2] = it
                }
                obj.fields.forEach {
                    val id2 = nextFieldId++
                    map["${obj.fullName}${UnlinkedObject.fieldSeparator}${it.name}"] = id2
                    map1[id2] = it.type
                }
            }
        }
        internalMappingCacheField = map1
        internalMappingCacheMethod = map2
        internalMappingCacheObject = map3
        internalMappingCache = map
        return map
    }

    fun getMapping(): Map<String, UInt> {
        if (mappingCache != null) {
            return mappingCache!!
        }
        val base = getSelfMapping()
        val map = mutableMapOf<String, UInt>()
        roots.filter { it.visibility == Visibility.PUBLIC }.forEach { pkg ->
            pkg.floating.filter { it.visibility == Visibility.PUBLIC }.forEach {
                map[it.fullName] = base[it.fullName]!!
            }
            pkg.objects.filter { it.visibility == Visibility.PUBLIC }.forEach { obj ->
                map[obj.fullName] = base[obj.fullName]!!
                obj.methods.filter { it.visibility == Visibility.PUBLIC }.forEach {
                    map[it.fullName] = base[it.fullName]!!
                }
                obj.fields.forEach {
                    val fieldName = "${obj.fullName}${UnlinkedObject.fieldSeparator}${it.name}"
                    map[fieldName] = base[fieldName]!!
                }
            }
        }
        mappingCache = map
        return map
    }

    fun asAPI(): UnlinkedContainer {
        TODO("Unlinked container with all code and private items deleted")
    }

    fun link(dependencies: Set<Container> = emptySet(), optimizers: List<Optimizer> = emptyList()): Container {
        var last = 1u
        val depMap = listOf(NameId(name, 0u), *dependencies.map { NameId(it.name, last++) }.toTypedArray())
        val map = getSelfMapping()
        val packages = mutableSetOf<LinkedPackage>()
        val stack = Stack<Type>()
        roots.forEach { unlinkedPackage ->
            val methods = mutableSetOf<Method>()
            val objects = mutableSetOf<Object>()
            unlinkedPackage.floating.forEach {
                methods.add(processMethod(stack, it, dependencies, map, optimizers, depMap))
            }
            unlinkedPackage.objects.forEach { unlinkedObject ->
                val methods2 = mutableSetOf<Method>()
                unlinkedObject.methods.forEach {
                    methods2.add(processMethod(stack, it, dependencies, map, optimizers, depMap))
                }
                val fieldNamePrefix = "${unlinkedObject.fullName}${UnlinkedObject.fieldSeparator}"
                if (unlinkedObject.visibility == Visibility.PRIVATE) {
                    objects.add(
                        PrivateObject(
                            map[unlinkedObject.fullName]!!,
                            unlinkedObject.fields.map { IdType(map["$fieldNamePrefix${it.name}"]!!, it.type) },
                            methods2.map { it as PrivateMethod })
                    )
                } else {
                    objects.add(
                        PublicObject(
                            map[unlinkedObject.fullName]!!,
                            unlinkedObject.name,
                            unlinkedObject.fields.map {
                                NamedIdType(
                                    name,
                                    map["$fieldNamePrefix${it.name}"]!!,
                                    it.type
                                )
                            },
                            methods2.toList()
                        )
                    )
                }
            }
            packages.add(LinkedPackage(methods.toList(), objects.toList()))
        }
        val entrypoint = if (entryPoint.isBlank()) {
            0u
        } else {
            map[entryPoint]!!
        }
        return LinkedContainer(name, entrypoint, depMap, packages.toList(), TODO(), TODO(), TODO())
    }

    private fun processMethod(
        stack: Stack<Type>,
        method: UnlinkedMethod,
        dependencies: Set<Container>,
        map: Map<String, UInt>,
        optimizers: List<Optimizer>,
        depMap: List<NameId>
    ): Method {
        stack.clear()
        val vars = mutableMapOf<CharSequence, UInt>()
        val types = mutableMapOf<UInt, Type>()
        var id = 0u
        val meth = method.copy() //prevent changes to the original code in case of reuse!
        meth.args.forEach {
            val i = id++
            vars[it.name] = i
            types[i] = it.type
        }
        meth.instructions.eval(dependencies, this, meth, stack, 0, vars, types, depMap)
        optimizers.forEach { it.visitSegment(meth.instructions) }
        val bulk = mutableListOf<Command>()
        meth.instructions.toBulk(bulk)
        return if (meth.visibility == Visibility.PRIVATE) {
            PrivateMethod(map[meth.fullName]!!, meth.args.size.toByte(), meth.returnType != Type.Unit, bulk)
        } else {
            PublicMethod(
                map[meth.fullName]!!,
                meth.name,
                meth.args.size.toByte(),
                meth.returnType != Type.Unit,
                bulk
            )
        }
    }

    fun newPackage(namespace: String, visibility: Visibility = Visibility.PRIVATE): UnlinkedPackage {
        val pkg = UnlinkedPackage(namespace, visibility)
        roots.add(pkg)
        return pkg
    }

    fun entryPoint(method: UnlinkedMethod) {
        check(method.parent == null) {
            "Entrypoint cannot have Object parent!"
        }
        check(method.args.size <= 1) {
            "Entrypoint cannot have more than 1 arg! Has ${method.args.size}"
        }
        /*check(method.args.isEmpty() || method.args.first().type is Type.Array) {
            "Entrypoint arg must be array if it exist! Was ${method.args.first().type}"
        }*/
        entryPoint(method.fullName)
    }

    fun entryPoint(namespace: String) {
        entryPoint = namespace
    }

    fun entryPoint(): String? {
        return entryPoint
    }

    override fun size(): Long {
        return 1 + MemorySegmentBuffer.sequenceSize(name) + MemorySegmentBuffer.sequenceSize(entryPoint) + MemorySegmentBuffer.varSize(
            roots.size
        ) + roots.sumOf { it.size() }
    }

    override fun save(buffer: MemorySegmentBuffer) {
        buffer.write(0.toByte())
        buffer.writeSequence(name)
        buffer.writeSequence(entryPoint)
        buffer.writeVar(roots.size)
        roots.forEach {
            it.save(buffer)
        }
    }

    override fun locateField(name: String): UInt? {
        return getMapping()[name]
    }

    override fun locateField(id: UInt): Type {
        return getFieldMapping()[id]!!
    }

    override fun locateMethod(name: String): UInt? {
        return getMapping()[name]
    }

    override fun locateMethod(id: UInt): Method {
        return getMethodMapping()[id]!!
    }

    override fun locateObject(name: String): UInt? {
        return getMapping()[name]
    }

    override fun locateObject(id: UInt): Object {
        return getObjectMapping()[id]!!
    }
}
