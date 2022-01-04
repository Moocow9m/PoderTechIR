package tech.poder.ir.vm

import tech.poder.ptir.PTIR
import java.lang.IllegalStateException

object VirtualMachine {
	private val enviornment = mutableMapOf<String, PTIR.Code>()
	private val global = mutableMapOf<UInt, Any>()

	fun resetEnv() {
		global.clear()
		enviornment.clear()
	}

	fun loadFile(code: PTIR.Code) {
		enviornment[code.id] = code
	}

	fun exec(code: PTIR.Code, method: UInt, vararg args: Any) {
		invoke(code.id, code.methods[code.methodIndex.indexOf(method)], args)
	}

	private fun getDataType(arg: Any, local: Map<UInt, Any>): Any? {
		when (arg) {
			is PTIR.Variable -> {
				return if (arg.local) {
					local[arg.index]
				} else {
					global[arg.index]
				}
			}
			else -> TODO(arg::class.java.name)
		}
	}

	private fun safeGetDataType(arg: Any, local: Map<UInt, Any>): Any {
		val tmp = getDataType(arg, local)
		check(tmp != null) {
			"[FATAL][VM] Variable was null on GET!"
		}

		return tmp
	}

	private fun setDataType(variable: PTIR.Variable, value: Any, local: MutableMap<UInt, Any>) {
		if (variable.local) {
			local[variable.index] = value
		} else {
			global[variable.index] = value
		}
	}

	private fun add(a: Any, b: Any): Any {
		return when {
			a is String || b is String -> a.toString() + b.toString()
			a is Float || b is Float -> a as Float + b as Float
			a is Int || b is Int -> a as Int + b as Int
			else -> throw IllegalStateException("[FATAL][VM] Invalid type for addition!")
		}
	}

	private fun invoke(name: String, method: PTIR.Method, args: Array<out Any>): Any? {
		val local = mutableMapOf<UInt, Any>()
		local[0u] = args
		var line = 0
		try {
			while (line < method.bytecode.size) {
				val op = method.bytecode[line]
				when (op.type) {
					PTIR.Op.RETURN -> {
						return if (op.args.isNotEmpty()) {
							safeGetDataType(op.args[0], local)
						} else {
							null
						}
					}
					PTIR.Op.THROW -> throw IllegalStateException("[FATAL][IR]" + op.args[0].toString()) //TODO caching
					PTIR.Op.LOOP -> TODO()
					PTIR.Op.BREAK -> TODO()
					PTIR.Op.GET_ARRAY_VAR -> TODO()
					PTIR.Op.SET_ARRAY_VAR -> TODO()
					PTIR.Op.SET_VAR -> TODO()
					PTIR.Op.GET_VAR -> TODO()
					PTIR.Op.GET_STRUCT_VAR -> TODO()
					PTIR.Op.SET_STRUCT_VAR -> TODO()
					PTIR.Op.NEW_ARRAY -> TODO()
					PTIR.Op.NEW_STRUCT -> TODO()
					PTIR.Op.COMPARE -> TODO()
					PTIR.Op.IF_NOT_EQUALS -> TODO()
					PTIR.Op.IF_EQUALS -> TODO()
					PTIR.Op.IF_LESS_THAN -> TODO()
					PTIR.Op.IF_GREATER_THAN -> TODO()
					PTIR.Op.ELSE -> TODO()
					PTIR.Op.REMAINDER -> TODO()
					PTIR.Op.DIVIDE -> TODO()
					PTIR.Op.MULTIPLY -> TODO()
					PTIR.Op.ADD -> {
						val a = safeGetDataType(op.args[0], local)
						val b = safeGetDataType(op.args[1], local)
						setDataType(op.args[0] as PTIR.Variable, add(a, b), local)
					}
					PTIR.Op.SUBTRACT -> TODO()
					PTIR.Op.AND -> TODO()
					PTIR.Op.OR -> TODO()
					PTIR.Op.SHL -> TODO()
					PTIR.Op.SHR -> TODO()
					PTIR.Op.SAR -> TODO()
					PTIR.Op.SAL -> TODO()
					PTIR.Op.INVOKE -> TODO()
					PTIR.Op.NULL -> TODO()
					PTIR.Op.IF_NULL -> TODO()
				}
				line++
			}
		} catch (ex: Exception) {
			val debugMessages = if (method.debugInfo != null) {
				method.debugInfo.filter { debug ->
					debug.methodLinesIndexes.let {
					it[0]..it[1]
				}.contains(line.toUInt()) }.map { it.methodLinesText }
			} else {
				emptyList()
			}
			println("Error on line $line of $name.ptir")
			if (debugMessages.isNotEmpty()) {
				println("\tDebug messages:")
				debugMessages.forEach { println("\t\t$it") }
			}
			throw ex
		}
		return null
	}
}