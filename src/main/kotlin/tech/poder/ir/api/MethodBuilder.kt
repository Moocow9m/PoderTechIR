package tech.poder.ir.api

import tech.poder.ptir.PTIR

data class MethodBuilder(
	val parent: CodeFile,
) {
	companion object {
		private var lastId = 0uL
	}
	private val bytecode: MutableList<PTIR.Expression> = mutableListOf()
	private val extraInfo: MutableList<PTIR.Info> = mutableListOf()
	private val debugInfo: MutableList<PTIR.Debug> = mutableListOf()
	val method: PTIR.Method = PTIR.Method(bytecode, extraInfo, debugInfo)
	internal var varId = 1u
	private val myId = lastId++

	val id by lazy {
		parent.idOf(this)
	}

	fun newLocal(): Variable {
		return Variable.newLocal(this)
	}

	fun newGlobal(): Variable {
		return Variable.newGlobal()
	}

	fun provideDebugInfo(debug: PTIR.Debug) {
		debugInfo.add(debug)
	}

	fun provideDebugLines(startLine: UInt, endLine: UInt = startLine, text: String) {
		debugInfo.add(PTIR.Debug(listOf(startLine, endLine), text))
	}

	fun addBreakPoint(line: Int, msg: String) {
		debugInfo.add(PTIR.Debug(listOf(line.toUInt()), msg, true))
	}

	private val placeholder = PTIR.Expression(PTIR.Op.DEFAULT)

	private fun addPlaceholder() {
		bytecode.add(placeholder)
	}

	internal fun addOp(op: PTIR.Op, store: Variable? = null, vararg args: Any) {
		val argsNoVars = args.map {
			when (it) {
				is Variable -> {
					it.toPTIR()
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
			bytecode.add(PTIR.Expression(op, listOf(Variable.VOID) + argsNoVars))
		} else {
			bytecode.add(PTIR.Expression(op, listOf(store.toPTIR()) + argsNoVars))
		}
	}

	fun compare(op: PTIR.Op, store: Variable, left: Any, right: Any) {
		addOp(op, store, left, right)
	}

	fun getArrayVar(array: Variable, index: Any, to: Variable) {
		addOp(PTIR.Op.GET_ARRAY_VAR, to, array, index)
	}

	fun setArrayVar(array: Variable, index: Any, from: Any) {
		addOp(PTIR.Op.SET_ARRAY_VAR, array, index, from)
	}

	fun getStructVar(structVar: Variable, struct: Struct, field: UInt, to: Variable) {
		val id = parent.registerOrAddStruct(struct)
		addOp(PTIR.Op.GET_STRUCT_VAR, to, structVar, field, id)
	}

	fun setStructVar(structVar: Variable, struct: Struct, field: UInt, from: Variable) {
		val id = parent.registerOrAddStruct(struct)
		addOp(PTIR.Op.SET_STRUCT_VAR, structVar, from, field, id)
	}

	fun setVar(to: Variable, value: Any) {
		addOp(PTIR.Op.SET_VAR, to, value)
	}

	fun getVar(from: Variable, to: Variable) {
		setVar(to, from)
		//addOp(PTIR.Op.GET_VAR, to, from)
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
		addPlaceholder()
		builder.invoke(this)
		val end = nextLineNumber()
		addOp(PTIR.Op.LOOP, condition, start + 1u, end - 1u)
		replaceLine(start, end)
	}

	fun break_() {
		addOp(PTIR.Op.BREAK)
	}

	fun and(left: Variable, right: Variable, store: Variable) {
		addOp(PTIR.Op.AND, store, left, right)
	}

	fun or(left: Variable, right: Variable, store: Variable) {
		addOp(PTIR.Op.OR, store, left, right)
	}

	fun xor(left: Variable, right: Variable, store: Variable) {
		addOp(PTIR.Op.XOR, store, left, right)
	}

	fun shl(left: Variable, right: Variable, store: Variable) {
		addOp(PTIR.Op.SHL, store, left, right)
	}

	fun shr(left: Variable, right: Variable, store: Variable) {
		addOp(PTIR.Op.SHR, store, left, right)
	}

	fun sar(left: Variable, right: Variable, store: Variable) {
		addOp(PTIR.Op.SAR, store, left, right)
	}

	fun sal(left: Variable, right: Variable, store: Variable) {
		addOp(PTIR.Op.SAL, store, left, right)
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

	private fun updateIf(startOfElse: UInt, endOfElse: UInt) {
		val index = bytecode.indexOfFirst { it.type.name.startsWith("IF") && it.args.last() == startOfElse }

		check(index > -1) {
			"Could not find matching IF statement for ELSE(${startOfElse}, ${endOfElse})!"
		}
		val target = bytecode.removeAt(index)
		val correctedArgs = target.args.toMutableList()
		correctedArgs.removeLast()
		correctedArgs.add(endOfElse + 1u)
		bytecode.add(index, PTIR.Expression(target.type, correctedArgs))
	}

	private fun replaceLine(target: UInt, with: UInt) {
		val index = target.toInt()
		val index2 = with.toInt()
		val get = bytecode[index2]
		bytecode.removeAt(index)
		bytecode.add(index, get)
		bytecode.removeAt(index2)
	}

	fun ifNull(check: Variable, if_: MethodBuilder.() -> Unit) {
		val start = nextLineNumber()
		addPlaceholder()
		if_.invoke(this)
		val end = nextLineNumber()
		addOp(PTIR.Op.IF_NULL, check, end, end) //last two args are: end Of IF location and after ELSE location
		replaceLine(start, end)
	}

	fun else_(block: MethodBuilder.() -> Unit) {
		val start = nextLineNumber()
		addPlaceholder()
		block.invoke(this)
		val end = nextLineNumber()
		addOp(PTIR.Op.ELSE, null, start, end - 1u)
		replaceLine(start, end)
		updateIf(start, end)
	}

	fun ifEquals(a: Variable, b: Any, if_: MethodBuilder.() -> Unit) {
		val start = nextLineNumber()
		addPlaceholder()
		if_.invoke(this)
		val end = nextLineNumber()
		addOp(PTIR.Op.IF_EQUALS, a, b, end - 1u, end)
		replaceLine(start, end)
	}

	fun ifLessThan(a: Variable, b: Any, if_: MethodBuilder.() -> Unit) {
		val start = nextLineNumber()
		addPlaceholder()
		if_.invoke(this)
		val end = nextLineNumber()
		addOp(PTIR.Op.IF_LESS_THAN, a, b, end - 1u, end)
		replaceLine(start, end)
	}

	fun ifGreaterThan(a: Variable, b: Any, if_: MethodBuilder.() -> Unit) {
		val start = nextLineNumber()
		addPlaceholder()
		if_.invoke(this)
		val end = nextLineNumber()
		addOp(PTIR.Op.IF_GREATER_THAN, a, b, end - 1u, end)
		replaceLine(start, end)
	}

	fun ifNotEquals(a: Variable, b: Any, if_: MethodBuilder.() -> Unit) {
		val start = nextLineNumber()
		addPlaceholder()
		if_.invoke(this)
		val end = nextLineNumber()
		addOp(PTIR.Op.IF_NOT_EQUALS, a, b, end - 1u, end)
		replaceLine(start, end)
	}

	override fun toString(): String {
		return "Method(id=$id, method=$method)"
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as MethodBuilder

		if (parent != other.parent) return false
		if (myId != other.myId) return false

		return true
	}

	override fun hashCode(): Int {
		var result = parent.hashCode()
		result = 31 * result + myId.hashCode()
		return result
	}
}
