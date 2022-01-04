package tech.poder.ir.v2.api

import tech.poder.ptir.PTIR
import java.io.OutputStream

data class CodeFile(val name: String) {
	private val methods: MutableList<MethodBuilder> = mutableListOf()
	private val structs: MutableSet<Struct> = mutableSetOf()
	internal var id = 0u
	fun addMethod(method: MethodBuilder.() -> Unit): UInt {
		val builder = MethodBuilder(this)
		method.invoke(builder)
		if (!methods.contains(builder)) {
			methods.add(builder)
		}
		return methods.indexOf(builder).toUInt()
	}

	fun write(out: OutputStream) {
		PTIR.Code(name, methods.map { it.method }, methods.map { it.id }, structs.map { it.types.toList() }).toBytes(out)
	}

	internal fun registerOrAddStruct(struct: Struct): UInt {
		if (!structs.contains(struct)) {
			structs.add(struct)
		}
		return structs.indexOf(struct).toUInt()
	}

	override fun hashCode(): Int {
		return name.hashCode()
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is CodeFile) return false

		return name == other.name
	}
}
