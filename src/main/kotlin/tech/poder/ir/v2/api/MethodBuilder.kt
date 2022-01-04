package tech.poder.ir.v2.api

import tech.poder.ptir.PTIR

data class MethodBuilder(
	val parent: CodeFile,
) {
	internal val id: UInt = parent.id++
	private val bytecode: MutableList<PTIR.Expression> = mutableListOf()
	private val extraInfo: MutableList<PTIR.Info> = mutableListOf()
	private val debugInfo: MutableList<PTIR.Debug> = mutableListOf()
	val method: PTIR.Method = PTIR.Method(bytecode, extraInfo, debugInfo)

	fun provideDebugInfo(debug: PTIR.Debug) {
		debugInfo.add(debug)
	}

	fun provideDebugLines(range: IntRange, text: String) {
		debugInfo.add(PTIR.Debug(listOf(range.first.toUInt(), range.last.toUInt()), text))
	}

	fun addBreakPoint(line: Int, msg: String) {
		debugInfo.add(PTIR.Debug(listOf(line.toUInt()), msg, true))
	}

	fun addOp(op: PTIR.Op, store: Variable? = null, vararg args: Any) {
		val argsNoVars = args.map {
			when (it) {
				is Variable -> {
					it.id
				}
				is Enum<*> -> {
					it.ordinal.toUInt()
				}
				else -> {
					it
				}
			}
		}
		if (store == null) {
			bytecode.add(PTIR.Expression(op, argsNoVars))
		} else {
			bytecode.add(PTIR.Expression(op, listOf(store.id) + argsNoVars))
		}
	}

	fun getArrayVar(array: Variable, index: Int, to: Variable) {
		addOp(PTIR.Op.GET_ARRAY_VAR, to, index, array)
	}

	fun setArrayVar(array: Variable, index: Int, from: Variable) {
		addOp(PTIR.Op.SET_ARRAY_VAR, array, index, from)
	}

	fun getStructVar(structVar: Variable, struct: Struct, field: UInt, to: Variable) {
		val id = parent.registerOrAddStruct(struct)
		addOp(PTIR.Op.GET_STRUCT_VAR, to, id, field, structVar)
	}

	fun setStructVar(structVar: Variable, struct: Struct, field: UInt, from: Variable) {
		val id = parent.registerOrAddStruct(struct)
		addOp(PTIR.Op.SET_STRUCT_VAR, structVar, id, field, from)
	}

	fun setVar(to: Variable, value: Any) {
		addOp(PTIR.Op.SET_VAR, to, value)
	}

	fun getVar(from: Variable, to: Variable) {
		addOp(PTIR.Op.GET_VAR, to, from)
	}

	fun newArray(store: Variable, size: UInt = 0u) {
		addOp(PTIR.Op.NEW_ARRAY, store, size)
	}

	fun newStruct(target: Variable, struct: Struct) {
		val id = parent.registerOrAddStruct(struct)
		addOp(PTIR.Op.NEW_STRUCT, target, id)
	}

	fun invoke(codeFile: CodeFile, method: UInt, store: Variable? = null, vararg args: Any) {
		if (codeFile.name == parent.name) {
			addOp(PTIR.Op.INVOKE, store, "", method, *args)
		} else {
			addOp(PTIR.Op.INVOKE, store, codeFile.name, method, *args)
		}
	}

	fun invoke(stdCall: PTIR.STDCall, store: Variable? = null, vararg args: Any) {
		addOp(PTIR.Op.INVOKE, store, stdCall, method, *args)
	}

	override fun toString(): String {
		return "Method(id=$id, method=$method)"
	}
}
