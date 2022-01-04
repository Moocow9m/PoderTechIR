package tech.poder.ir.data.base.unlinked

import tech.poder.ir.data.CodeBuilder
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Object
import tech.poder.ir.metadata.NamedType
import tech.poder.ir.metadata.Visibility
import tech.poder.ir.util.MemorySegmentBuffer

data class UnlinkedObject internal constructor(
	val parent: UnlinkedPackage,
	val name: String,
	val visibility: Visibility = Visibility.PRIVATE,
	internal val methods: MutableSet<UnlinkedMethod>,
	internal val fields: List<NamedType>
) : Object {

	companion object {
		const val objectSeparator = '^'
		const val fieldSeparator = '$'
	}

	init {
		check(name.isNotBlank()) {
			"Namespace cannot be blank!"
		}
		check(!name.contains(objectSeparator)) {
			"Namespace cannot contain $objectSeparator!"
		}
		check(!name.contains(fieldSeparator)) {
			"Namespace cannot contain $fieldSeparator!"
		}
		check(!name.contains(UnlinkedMethod.methodSeparator)) {
			"Namespace cannot contain ${UnlinkedMethod.methodSeparator}!"
		}
	}

	val fullName by lazy {
		"${parent.namespace}$objectSeparator$name"
	}

	fun newMethod(
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
		val meth = UnlinkedMethod(
			parent,
			this,
			name,
			returnType,
			setOf(NamedType("this", Type.Struct(fullName, fields)), *args.toTypedArray()),
			trueVis
		)
		val builder = CodeBuilder(meth)
		code.invoke(builder)
		builder.finalize()
		methods.add(meth)
		return meth
	}

	override fun size(): Long {
		return 2L + MemorySegmentBuffer.sequenceSize(name) + MemorySegmentBuffer.varSize(fields.size) + fields.sumOf { it.size() } + MemorySegmentBuffer.varSize(
			methods.size
		) + methods.sumOf { it.size() }
	}

	override fun save(buffer: MemorySegmentBuffer) {
		buffer.write(0.toByte())
		buffer.write(visibility.ordinal.toByte())
		buffer.writeSequence(name)
		buffer.writeVar(fields.size)
		fields.forEach {
			it.toBin(buffer)
		}
		buffer.writeVar(methods.size)
		methods.forEach {
			it.save(buffer)
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is UnlinkedObject) return false

		if (parent != other.parent) return false
		if (name != other.name) return false

		return true
	}

	override fun hashCode(): Int {
		var result = parent.hashCode()
		result = 31 * result + name.hashCode()
		return result
	}

	override fun toString(): String {
		return (
				"""
            $visibility class $fullName {        
                ${fields.joinToString("\n\t")}
                
                ${methods.joinToString("\n\t")}
            }
            """.trimIndent()
				)
	}

	fun toString(tabCount: Int): String {
		return toString().prependIndent("\t".repeat(tabCount))
		/*
		val tabs = "\t".repeat(tabCount)

		return (
			"""
			$tabs$visibility class $fullName {
			$tabs${fields.joinToString("\n") { it.toString(tabCount + 1) }}


			$tabs${methods.joinToString("\n") { it.toString(tabCount + 1) }}

			$tabs}
			""".trimIndent()
		)
		*/
	}
}
