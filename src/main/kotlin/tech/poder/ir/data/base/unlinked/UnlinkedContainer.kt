package tech.poder.ir.data.base.unlinked

import tech.poder.ir.api.Optimizer
import tech.poder.ir.commands.Command
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Container
import tech.poder.ir.data.base.Method
import tech.poder.ir.data.base.Object
import tech.poder.ir.data.base.linked.*
import tech.poder.ir.metadata.Visibility
import tech.poder.ir.util.MemorySegmentBuffer
import java.util.*

class UnlinkedContainer(override val name: String) : Container {
    internal val roots: MutableSet<UnlinkedPackage> = mutableSetOf()
    internal var entryPoint: String = ""
    private var mappingCache: Map<String, UInt>? = null
    private var internalMappingCache: Map<String, UInt>? = null

    internal fun getSelfMapping(): Map<String, UInt> { //private items will have their name-dropped from binary on link!
        if (internalMappingCache != null) {
            return internalMappingCache!!
        }
        var nextMethodId = 1u //start at 1 so 0 can be null for entryPoint
        var nextObjectId = 0u
        var nextFieldId = 0u
        val map = mutableMapOf<String, UInt>()
        roots.forEach { pkg ->
            pkg.floating.forEach {
                map[it.fullName] = nextMethodId++
            }
            pkg.objects.forEach { obj ->
                map[obj.fullName] = nextObjectId++
                obj.methods.forEach {
                    map[it.fullName] = nextMethodId++
                }
                obj.fields.forEach {
                    map["${obj.fullName}${UnlinkedObject.fieldSeparator}${it.name}"] = nextFieldId++
                }
            }
        }
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

    fun link(dependencies: Set<Container> = emptySet(), optimizers: List<Optimizer> = emptyList()): Container {
        val map = getSelfMapping()
        val packages = mutableSetOf<LinkedPackage>()
        val stack = Stack<Type>()
        roots.forEach { unlinkedPackage ->
            val methods = mutableSetOf<Method>()
            val objects = mutableSetOf<Object>()
            unlinkedPackage.floating.forEach {
                methods.add(processMethod(stack, it, dependencies, map, optimizers))
            }
            unlinkedPackage.objects.forEach { unlinkedObject ->
                val methods2 = mutableSetOf<Method>()
                unlinkedObject.methods.forEach {
                    methods2.add(processMethod(stack, it, dependencies, map, optimizers))
                }
                if (unlinkedObject.visibility == Visibility.PRIVATE) {
                    objects.add(
                        PrivateObject(
                            map[unlinkedObject.fullName]!!,
                            unlinkedObject.fields.map { it.type },
                            methods2.map { it as PrivateMethod })
                    )
                } else {
                    objects.add(
                        PublicObject(
                            map[unlinkedObject.fullName]!!,
                            unlinkedObject.name,
                            unlinkedObject.fields,
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
        return LinkedContainer(name, entrypoint, packages.toList())
    }

    private fun processMethod(
        stack: Stack<Type>,
        method: UnlinkedMethod,
        dependencies: Set<Container>,
        map: Map<String, UInt>,
        optimizers: List<Optimizer>
    ): Method {
        stack.clear()
        val vars = mutableMapOf<CharSequence, UInt>()
        val types = mutableMapOf<UInt, Type>()
        var id = 0u
        method.args.forEach {
            val i = id++
            vars[it.name] = i
            types[i] = it.type
        }
        method.instructions.eval(dependencies, this, method, stack, 0, vars, types)
        optimizers.forEach { it.visitSegment(method.instructions) }
        val bulk = mutableListOf<Command>()
        method.instructions.toBulk(bulk)
        return if (method.visibility == Visibility.PRIVATE) {
            PrivateMethod(map[method.fullName]!!, method.args.size.toByte(), method.returnType != Type.Unit, bulk)
        } else {
            PublicMethod(
                map[method.fullName]!!,
                method.name,
                method.args.size.toByte(),
                method.returnType != Type.Unit,
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
}
