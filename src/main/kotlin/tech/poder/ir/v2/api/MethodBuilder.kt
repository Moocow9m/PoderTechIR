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
		addOp(PTIR.Op.INVOKE, store, stdCall, *args)
	}

	fun return_(value: Any? = null) {
		if (value == null) {
			addOp(PTIR.Op.RETURN)
		} else {
			addOp(PTIR.Op.RETURN, null, value)
		}
	}

	fun throw_(value: String) {
		addOp(PTIR.Op.THROW, null, value)
	}

	fun nextLineNumber(): UInt {
		return (bytecode.size).toUInt()
	}

	fun loop(condition: Variable, builder: MethodBuilder.() -> Unit) {
		val start = nextLineNumber()
		builder.invoke(this)
		val end = nextLineNumber() - 1u
		addOp(PTIR.Op.LOOP, condition, start, end)
	}

	fun break_() {
		addOp(PTIR.Op.BREAK)
	}

	fun add(store: Variable, a: Any, b: Any) {
		addOp(PTIR.Op.ADD, store, a, b)
	}

	fun subtract(store: Variable, a: Any, b: Any) {
		addOp(PTIR.Op.SUBTRACT, store, a, b)
	}

	fun multiply(store: Variable, a: Any, b: Any) {
		addOp(PTIR.Op.MULTIPLY, store, a, b)
	}

	fun divide(store: Variable, a: Any, b: Any) {
		addOp(PTIR.Op.DIVIDE, store, a, b)
	}

	fun modulo(store: Variable, a: Any, b: Any) {
		addOp(PTIR.Op.REMAINDER, store, a, b)
	}

	fun setNull(store: Variable) {
		addOp(PTIR.Op.NULL, store)
	}

	fun ifNull(store: Variable, check: Variable) {
		addOp(PTIR.Op.IF_NULL, store, check)
	}

	fun ifEquals(store: Variable, a: Variable, b: Variable) {
		addOp(PTIR.Op.IF_EQUALS, store, a, b)
	}

	fun ifLessThan(store: Variable, a: Variable, b: Variable) {
		addOp(PTIR.Op.IF_LESS_THAN, store, a, b)
	}

	fun ifGreaterThan(store: Variable, a: Variable, b: Variable) {
		addOp(PTIR.Op.IF_GREATER_THAN, store, a, b)
	}

	fun ifNot(store: Variable, check: Variable) {
		addOp(PTIR.Op.NOT, store, check)
	}

	override fun toString(): String {
		return "Method(id=$id, method=$method)"
	}
}
