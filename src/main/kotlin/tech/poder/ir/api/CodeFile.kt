package tech.poder.ir.api

import tech.poder.ptir.PTIR
import java.io.OutputStream

data class CodeFile(val name: String) {
	private val methods: MutableList<MethodBuilder> = mutableListOf()
	private val structs: MutableSet<Struct> = mutableSetOf()
	internal var id = 0u
	fun addMethod(method: MethodBuilder.() -> Unit): UInt {
		val builder = addMethodStub()
		fromMethodStub(builder, method)
		return builder
	}

	fun addMethodStub(): UInt {
		val builder = MethodBuilder(this)
		if (!methods.contains(builder)) {
			methods.add(builder)
		}
		return builder.getId()
	}

	fun fromMethodStub(id: UInt, block: MethodBuilder.() -> Unit) {
		val method = methods.find { it.getId() == id }!!
		block.invoke(method)
	}

	fun asCode(): PTIR.Code {
		return PTIR.Code(name, methods.map { it.method }, methods.map { it.getId() }, structs.map { it.types.toList() })
	}

	fun asHeader(): PTIR.Code {
		return PTIR.Code(name)
	}

	fun write(out: OutputStream) {
		asCode().toBytes(out)
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
