package tech.poder.ir.v2.api

import tech.poder.ptir.PTIR

data class MethodBuilder(
	val parent: CodeFile,
	internal val id: UInt = parent.id++,
	private val bytecode: MutableList<PTIR.Expression> = mutableListOf(),
	private val extraInfo: MutableList<PTIR.Info> = mutableListOf(),
	private val debugInfo: MutableList<PTIR.Debug> = mutableListOf(),
	val method: PTIR.Method = PTIR.Method(bytecode, extraInfo, debugInfo)
) {

	fun provideDebugInfo(debug: PTIR.Debug) {
		debugInfo.add(debug)
	}

	fun provideDebugLines(range: IntRange, text: String) {
		debugInfo.add(PTIR.Debug(listOf(range.first.toUInt(), range.last.toUInt()), text))
	}

	fun addBreakPoint(line: Int, msg: String) {
		debugInfo.add(PTIR.Debug(listOf(line.toUInt()), msg, true))
	}

	fun addOp(op: PTIR.Op, vararg args: Any) {
		bytecode.add(PTIR.Expression(op, args.toList()))
	}

	fun getArrayVar(target: Varible, index: Int) {
		addOp(PTIR.Op.GET_ARRAY_VAR, target, index)
	}

	fun setArrayVar(target: Varible, index: Int) {
		addOp(PTIR.Op.SET_ARRAY_VAR, target, index)
	}

	fun getStructVar(target: Varible, struct: Struct, field: UInt) {
		val id = parent.registerOrAddStruct(struct)
		addOp(PTIR.Op.SET_STRUCT_VAR, target, id, field)
	}

	fun setStructVar(target: Varible, struct: Struct, field: UInt) {
		val id = parent.registerOrAddStruct(struct)
		addOp(PTIR.Op.SET_STRUCT_VAR, target, id, field)
	}

	fun setVar(target: Varible, value: Any) {
		addOp(PTIR.Op.SET_VAR, target, value)
	}

	fun newArray(target: Varible, size: Int = -1) {
		addOp(PTIR.Op.NEW_ARRAY, target, size)
	}

	fun newStruct(target: Varible, struct: Struct) {
		val id = parent.registerOrAddStruct(struct)
		addOp(PTIR.Op.NEW_STRUCT, target, id)
	}

	override fun toString(): String {
		return "Method(id=$id, method=$method)"
	}
}
