package tech.poder.ir.data.base.unlinked

import tech.poder.ir.api.Optimizer
import tech.poder.ir.commands.Command
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Container
import tech.poder.ir.data.base.Method
import tech.poder.ir.data.base.Object
import tech.poder.ir.data.base.api.APIContainer
import tech.poder.ir.data.base.api.PublicMethod
import tech.poder.ir.data.base.api.PublicObject
import tech.poder.ir.data.base.linked.LinkedContainer
import tech.poder.ir.metadata.NameId
import tech.poder.ir.metadata.Visibility
import tech.poder.ir.util.MemorySegmentBuffer
import java.util.*

class UnlinkedContainer(override val name: String) : Container {
	private val roots: MutableSet<UnlinkedPackage> = mutableSetOf()
	private var entryPoint: String = ""
	private var mappingCache: Map<String, UInt>? = null
	private var internalMappingCache: Map<String, UInt>? = null
	private var internalMappingCacheMethod: Map<UInt, UnlinkedMethod>? = null
	private var internalMappingCacheObject: Map<UInt, UnlinkedObject>? = null

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
		var nextMethodId = 0u
		var nextObjectId = 0u
		val map = mutableMapOf<String, UInt>()
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
			}
		}
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

	fun linkAndOptimize(
		dependencies: Set<APIContainer> = emptySet(),
		optimizers: List<Optimizer> = emptyList()
	): Pair<APIContainer, LinkedContainer> {
		var last = 1u
		val depMap = listOf(NameId(name, 0u), *dependencies.map { NameId(it.name, last++) }.toTypedArray())
		val map = getSelfMapping()

		val stack = Stack<Type>()

		val methods = mutableSetOf<PublicMethod>()
		val codes = mutableListOf<List<Command>>()
		val objects = mutableSetOf<PublicObject>()
		val structs = mutableListOf<List<Type>>()

		roots.filter { it.visibility == Visibility.PUBLIC }.forEach { pkg ->
			pkg.floating.filter { it.visibility == Visibility.PUBLIC }.forEach {
				val methPair = processMethod(stack, it, dependencies, optimizers, depMap)
				methods.add(methPair.first!!)
				codes.add(methPair.second)
			}
			pkg.objects.filter { it.visibility == Visibility.PUBLIC }.forEach { obj ->
				objects.add(PublicObject(obj.fullName, obj.fields))
				structs.add(obj.fields.map { it.type })
				obj.methods.filter { it.visibility == Visibility.PUBLIC }.forEach {
					val methPair = processMethod(stack, it, dependencies, optimizers, depMap)
					methods.add(methPair.first!!)
					codes.add(methPair.second)
				}
			}
		}
		roots.filter { it.visibility == Visibility.PRIVATE }.forEach { pkg ->
			pkg.floating.filter { it.visibility == Visibility.PRIVATE }.forEach {
				val methPair = processMethod(stack, it, dependencies, optimizers, depMap)
				codes.add(methPair.second)
			}
			pkg.objects.filter { it.visibility == Visibility.PRIVATE }.forEach { obj ->
				structs.add(obj.fields.map { it.type })
				obj.methods.filter { it.visibility == Visibility.PRIVATE }.forEach {
					val methPair = processMethod(stack, it, dependencies, optimizers, depMap)
					codes.add(methPair.second)
				}
			}
		}
		val entrypoint = if (entryPoint.isBlank()) {
			-1
		} else {
			map[entryPoint]!!.toInt()
		}
		val api = APIContainer(name, entrypoint, methods.toList(), objects.toList())
		val bin = LinkedContainer(name, entrypoint, depMap.sortedBy { it.id }.map { it.name }, codes, structs)
		return Pair(api, bin)
	}

	private fun processMethod(
		stack: Stack<Type>,
		method: UnlinkedMethod,
		dependencies: Set<APIContainer>,
		optimizers: List<Optimizer>,
		depMap: List<NameId>
	): Pair<PublicMethod?, List<Command>> {
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
		return Pair(
			if (meth.visibility == Visibility.PRIVATE) {
				null
			} else {
				PublicMethod(
					meth.fullName,
					meth.args.map { it.type },
					meth.returnType
				)
			}, bulk
		)
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

	override fun locateField(obj: UInt, name: String): UInt? {
		return getMapping()[name]
	}

	override fun locateField(obj: UInt, id: UInt): Type {
		return getObjectMapping()[obj]!!.fields[id.toInt()].type
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
