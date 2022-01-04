package tech.poder.ir.data.base.unlinked

import tech.poder.ir.data.CodeBuilder
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Package
import tech.poder.ir.metadata.NamedType
import tech.poder.ir.metadata.Visibility
import tech.poder.ir.util.MemorySegmentBuffer

data class UnlinkedPackage internal constructor(
	val namespace: String,
	val visibility: Visibility = Visibility.PRIVATE,
	internal val objects: MutableSet<UnlinkedObject> = mutableSetOf(),
	internal val floating: MutableSet<UnlinkedMethod> = mutableSetOf(),
) : Package {

	init {
		check(namespace.isNotBlank()) {
			"Namespace cannot be blank!"
		}
		check(!namespace.contains(UnlinkedObject.objectSeparator)) {
			"Namespace cannot contain ${UnlinkedObject.objectSeparator}!"
		}
		check(!namespace.contains(UnlinkedObject.fieldSeparator)) {
			"Namespace cannot contain ${UnlinkedObject.fieldSeparator}!"
		}
		check(!namespace.contains(UnlinkedMethod.methodSeparator)) {
			"Namespace cannot contain ${UnlinkedMethod.methodSeparator}!"
		}
	}

	fun newFloatingMethod(
		name: String,
		vis: Visibility = Visibility.PRIVATE,
		returnType: Type = Type.Unit,
		args: Set<NamedType> = emptySet(),
		code: (CodeBuilder) -> Unit
	): UnlinkedMethod {
		val trueVis = if (visibility == Visibility.PRIVATE) {
			Visibility.PRIVATE
		} else {
			vis
		}
		val meth = UnlinkedMethod(this, null, name, returnType, args, trueVis)
		val builder = CodeBuilder(meth)
		code.invoke(builder)
		builder.finalize()
		floating.add(meth)
		return meth
	}

	fun newObject(
		name: String,
		vis: Visibility = Visibility.PRIVATE,
		fields: Set<NamedType> = emptySet()
	): UnlinkedObject {
		val trueVis = if (visibility == Visibility.PRIVATE) {
			Visibility.PRIVATE
		} else {
			vis
		}
		return UnlinkedObject(
			this,
			name,
			trueVis,
			mutableSetOf(),
			fields.map { NamedType("${namespace}.$name.${it.name}", it.type) }
		).apply {
			objects.add(this)
		}
	}

	fun finalize() {
		//todo this will remove names from all methods and objects marked private and replace the references with internal id numbers
	}

	override fun hashCode(): Int {
		return namespace.hashCode()
	}


	override fun toString(): String {
		return (
				"""
            $visibility package $namespace {
                   ${floating.joinToString("\n\t")}
                   ${objects.joinToString("\n") { it.toString(1) }}
            }       
            """.trimIndent()
				)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is UnlinkedPackage) return false

		if (namespace != other.namespace) return false

		return true
	}

	override fun size(): Long {
		return 2L + MemorySegmentBuffer.sequenceSize(namespace) + MemorySegmentBuffer.varSize(objects.size) + objects.sumOf { it.size() } + MemorySegmentBuffer.varSize(
			floating.size
		) + floating.sumOf { it.size() }
	}

	override fun save(buffer: MemorySegmentBuffer) {
		buffer.write(0.toByte())
		buffer.write(visibility.ordinal.toByte())
		buffer.writeSequence(namespace)
		buffer.writeVar(objects.size)
		objects.forEach {
			it.save(buffer)
		}
		buffer.writeVar(floating.size)
		floating.forEach {
			it.save(buffer)
		}
	}
}
