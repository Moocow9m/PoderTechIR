package tech.poder.ir.api

import tech.poder.proto.BitInputStream
import tech.poder.proto.BitOutputStream
import tech.poder.ptir.PTIR

data class CodeFile(val name: String) {
	companion object {
		fun resetEnv() {
			Variable.resetEnv() //for shared states to be disconnected
		}

		fun presetEnv(startingVariable: UInt = 1u) {
			check(startingVariable > 0u) { "startingVariable must be greater than 0!" }
			Variable.presetEnv(startingVariable)
		}

		fun read(inputStream: BitInputStream): PTIR.Code {
			return PTIR.Code.fromBytes(inputStream)
		}
	}

	private val methods: MutableList<MethodBuilder> = mutableListOf(MethodBuilder(this)) //always add initial method
	private val structs: MutableSet<Struct> = mutableSetOf()

	fun addMethod(method: MethodBuilder.() -> Unit): UInt {
		val builder = addMethodStub()
		fromMethodStub(builder, method)
		return builder
	}

	fun addMethodStub(): UInt {
		val builder = MethodBuilder(this)
		methods.add(builder)
		return builder.id
	}

	internal fun idOf(methodBuilder: MethodBuilder): UInt {
		return methods.indexOf(methodBuilder).toUInt()
	}

	fun fromMethodStub(id: UInt, block: MethodBuilder.() -> Unit) {
		val method = methods.find { it.id == id }!!
		block.invoke(method)
	}

	fun asCode(): PTIR.Code {
		return PTIR.Code(name, methods.map { it.method }, Variable.lastGlobalId(), structs.map { it.types.toList() })
	}

	fun asHeader(): PTIR.Code {
		return PTIR.Code(name)
	}

	fun write(out: BitOutputStream) {
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

	override fun toString(): String {
		return "CodeFile(name='$name', methods=[${methods.joinToString(", ")}], structs=[${structs.joinToString(", ")}])"
	}
}
