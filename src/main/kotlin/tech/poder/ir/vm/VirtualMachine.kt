package tech.poder.ir.vm

import tech.poder.ptir.PTIR
import kotlin.experimental.xor

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

	private fun setDataType(variable: PTIR.Variable, value: Any?, local: MutableMap<UInt, Any>) {
		if (value == null) {
			if (variable.local) {
				local.remove(variable.index)
			} else {
				global.remove(variable.index)
			}
		} else {
			if (variable.local) {
				local[variable.index] = value
			} else {
				global[variable.index] = value
			}
		}
	}

	private fun typeSize(target: Any): Int {
		return when (target) {
			is Byte -> 0
			is Short -> 5
			is Int -> 10
			is Long -> 15
			is Float -> 20
			is Double -> 25
			is UByte -> 1
			is UShort -> 6
			is UInt -> 11
			is ULong -> 16
			is String -> 100
			is List<*> -> 200
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${target::class.java.name}")
		}
	}

	private fun add(a: Number, b: Number): Number {
		return when (a) {
			is Byte -> a + b.toByte()
			is Short -> a + b.toShort()
			is Int -> a + b.toInt()
			is Long -> a + b.toLong()
			is Float -> a + b.toFloat()
			is Double -> a + b.toDouble()
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${a::class.java.name}")
		}
	}

	private fun sub(a: Number, b: Number): Number {
		return when (a) {
			is Byte -> a - b.toByte()
			is Short -> a - b.toShort()
			is Int -> a - b.toInt()
			is Long -> a - b.toLong()
			is Float -> a - b.toFloat()
			is Double -> a - b.toDouble()
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${a::class.java.name}")
		}
	}

	private fun mul(a: Number, b: Number): Number {
		return when (a) {
			is Byte -> a * b.toByte()
			is Short -> a * b.toShort()
			is Int -> a * b.toInt()
			is Long -> a * b.toLong()
			is Float -> a * b.toFloat()
			is Double -> a * b.toDouble()
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${a::class.java.name}")
		}
	}

	private fun div(a: Number, b: Number): Number {
		return when (a) {
			is Byte -> a / b.toByte()
			is Short -> a / b.toShort()
			is Int -> a / b.toInt()
			is Long -> a / b.toLong()
			is Float -> a / b.toFloat()
			is Double -> a / b.toDouble()
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${a::class.java.name}")
		}
	}

	private fun mod(a: Number, b: Number): Number {
		return when (a) {
			is Byte -> a % b.toByte()
			is Short -> a % b.toShort()
			is Int -> a % b.toInt()
			is Long -> a % b.toLong()
			is Float -> a % b.toFloat()
			is Double -> a % b.toDouble()
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${a::class.java.name}")
		}
	}

	private fun shr(a: Number, b: Number): Number {
		return when (a) {
			is Byte -> (a.toInt() ushr b.toInt()).toByte()
			is Short -> (a.toInt() ushr b.toInt()).toShort()
			is Int -> a ushr b.toInt()
			is Long -> a ushr b.toInt()
			is Float, is Double -> throw IllegalStateException("[FATAL][VM] ILLEGAL Shift Type! ${a::class.java.name}")
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${a::class.java.name}")
		}
	}

	private fun sar(a: Number, b: Number): Number {
		return when (a) {
			is Byte -> (a.toInt() shr b.toInt()).toByte()
			is Short -> (a.toInt() shr b.toInt()).toShort()
			is Int -> a shr b.toInt()
			is Long -> a shr b.toInt()
			is Float, is Double -> throw IllegalStateException("[FATAL][VM] ILLEGAL Shift Type! ${a::class.java.name}")
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${a::class.java.name}")
		}
	}

	private fun sal(a: Number, b: Number): Number {
		return when (a) {
			is Byte -> (a.toInt() shl b.toInt()).toByte()
			is Short -> (a.toInt() shl b.toInt()).toShort()
			is Int -> a shl b.toInt()
			is Long -> a shl b.toInt()
			is Float, is Double -> throw IllegalStateException("[FATAL][VM] ILLEGAL Shift Type! ${a::class.java.name}")
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${a::class.java.name}")
		}
	}

	private fun xor(a: Number, b: Number): Number {
		return when (a) {
			is Byte -> a xor b.toByte()
			is Short -> a xor b.toShort()
			is Int -> a xor b.toInt()
			is Long -> a xor b.toLong()
			is Float, is Double -> throw IllegalStateException("[FATAL][VM] ILLEGAL Shift Type! ${a::class.java.name}")
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${a::class.java.name}")
		}
	}

	private fun toUnsigned(a: Any): ULong { //Unfortunately, this is the only way to do this as Unsigned numbers do not share an Interface like Number does
		return when (a) {
			is Number -> a.toLong().toULong()
			is ULong -> a
			is UInt -> a.toULong()
			is UShort -> a.toULong()
			is UByte -> a.toULong()
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${a::class.java.name}")
		}
	}

	private fun toSigned(a: Any): Number {
		return when (a) {
			is Number -> a
			is ULong -> a.toLong()
			is UInt -> a.toLong()
			is UShort -> a.toInt()
			is UByte -> a.toShort()
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${a::class.java.name}")
		}
	}

	private fun add(a: Any, b: Any): Any {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		val second = if (first === a) {
			b
		} else {
			a
		}
		return when (first) {
			is String -> first + second.toString()
			is ULong -> {
				first + toUnsigned(second)
			}
			is UInt -> {
				first + toUnsigned(second).toUInt()
			}
			is UShort -> {
				first + toUnsigned(second).toUShort()
			}
			is UByte -> {
				first + toUnsigned(second).toUByte()
			}
			is Number -> add(first, second as Number)
			is List<*> -> TODO("LIST ADD")
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for addition! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun sub(a: Any, b: Any): Any {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		val second = if (first === a) {
			b
		} else {
			a
		}
		return when (first) {
			is String -> TODO("STRING SUB")
			is ULong -> {
				first - toUnsigned(second)
			}
			is UInt -> {
				first - toUnsigned(second).toUInt()
			}
			is UShort -> {
				first - toUnsigned(second).toUShort()
			}
			is UByte -> {
				first - toUnsigned(second).toUByte()
			}
			is Number -> sub(first, second as Number)
			is List<*> -> TODO("LIST ADD")
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for subtraction! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun mul(a: Any, b: Any): Any {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		val second = if (first === a) {
			b
		} else {
			a
		}
		return when (first) {
			is ULong -> {
				first * toUnsigned(second)
			}
			is UInt -> {
				first * toUnsigned(second).toUInt()
			}
			is UShort -> {
				first * toUnsigned(second).toUShort()
			}
			is UByte -> {
				first * toUnsigned(second).toUByte()
			}
			is Number -> mul(first, second as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for multiplication! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun div(a: Any, b: Any): Any {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		val second = if (first === a) {
			b
		} else {
			a
		}
		return when (first) {
			is ULong -> {
				first / toUnsigned(second)
			}
			is UInt -> {
				first / toUnsigned(second).toUInt()
			}
			is UShort -> {
				first / toUnsigned(second).toUShort()
			}
			is UByte -> {
				first / toUnsigned(second).toUByte()
			}
			is Number -> div(first, second as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for division! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun mod(a: Any, b: Any): Any {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		val second = if (first === a) {
			b
		} else {
			a
		}
		return when (first) {
			is ULong -> {
				first % toUnsigned(second)
			}
			is UInt -> {
				first % toUnsigned(second).toUInt()
			}
			is UShort -> {
				first % toUnsigned(second).toUShort()
			}
			is UByte -> {
				first % toUnsigned(second).toUByte()
			}
			is Number -> mod(first, second as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for division! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun shr(a: Any, b: Any): Any {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		val second = if (first === a) {
			b
		} else {
			a
		}
		return when (first) {
			is UInt -> { //Unknown effect....
				(first.toInt() ushr toSigned(second).toInt()).toUInt()
			}
			is UShort -> {
				(first.toInt() ushr toSigned(second).toInt()).toUShort()
			}
			is UByte -> {
				(first.toInt() ushr toSigned(second).toInt()).toUByte()
			}
			is Number -> shr(first, second as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for SHR! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun sar(a: Any, b: Any): Any {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		val second = if (first === a) {
			b
		} else {
			a
		}
		return when (first) {
			is ULong -> {
				first shr toSigned(second).toInt()
			}
			is UInt -> {
				first shr toSigned(second).toInt()
			}
			is UShort -> {
				(first.toUInt() shr toSigned(second).toInt()).toUShort()
			}
			is UByte -> {
				(first.toUInt() shr toSigned(second).toInt()).toUByte()
			}
			is Number -> sar(first, second as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for SAR! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun sal(a: Any, b: Any): Any {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		val second = if (first === a) {
			b
		} else {
			a
		}
		return when (first) {
			is ULong -> {
				first shl toSigned(second).toInt()
			}
			is UInt -> {
				first shl toSigned(second).toInt()
			}
			is UShort -> {
				(first.toUInt() shl toSigned(second).toInt()).toUShort()
			}
			is UByte -> {
				(first.toUInt() shl toSigned(second).toInt()).toUByte()
			}
			is Number -> sal(first, second as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for SAL! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun xor(a: Any, b: Any): Any {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		val second = if (first === a) {
			b
		} else {
			a
		}
		return when (first) {
			is ULong -> {
				first xor toUnsigned(second)
			}
			is UInt -> {
				first xor toUnsigned(second).toUInt()
			}
			is UShort -> {
				first xor toUnsigned(second).toUShort()
			}
			is UByte -> {
				first xor toUnsigned(second).toUByte()
			}
			is Number -> xor(first, second as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for SAL! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun readBool(data: Any): Boolean {
		return when (data) {
			is Number -> data != 0
			is Boolean -> data
			is ULong -> data != 0u
			is UInt -> data != 0u
			is UShort -> data != 0u
			is UByte -> data != 0u
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for Bool! ${data::class.java.name}")
		}
	}

	private fun invoke(name: String, method: PTIR.Method, args: Array<out Any>): Any? {
		val local = mutableMapOf<UInt, Any>()
		local[0u] = args
		var line = 0
		try {
			while (line < method.bytecode.size) {
				val loop = LoopState()
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
					PTIR.Op.BREAK -> {
						line = loop.end
						loop.condition = null
					}
					PTIR.Op.GET_ARRAY_VAR -> TODO()
					PTIR.Op.SET_ARRAY_VAR -> TODO()
					PTIR.Op.SET_VAR -> {
						val a = safeGetDataType(op.args[1], local)
						setDataType(op.args[0] as PTIR.Variable, a, local)
					}
					PTIR.Op.GET_VAR -> {
						val a = safeGetDataType(op.args[1], local)
						setDataType(op.args[0] as PTIR.Variable, a, local)
					}
					PTIR.Op.GET_STRUCT_VAR -> TODO()
					PTIR.Op.SET_STRUCT_VAR -> TODO()
					PTIR.Op.NEW_ARRAY -> TODO()
					PTIR.Op.NEW_STRUCT -> TODO()
					PTIR.Op.COMPARE -> TODO()
					PTIR.Op.IF_NOT_EQUALS -> TODO()
					PTIR.Op.IF_EQUALS -> TODO()
					PTIR.Op.IF_LESS_THAN -> TODO()
					PTIR.Op.IF_GREATER_THAN -> TODO()
					PTIR.Op.IF_NULL -> TODO()
					PTIR.Op.ELSE -> {
						/*no op*/
					}
					PTIR.Op.REMAINDER -> {
						val a = safeGetDataType(op.args[0], local)
						val b = safeGetDataType(op.args[1], local)
						setDataType(op.args[0] as PTIR.Variable, mod(a, b), local)
					}
					PTIR.Op.DIVIDE -> {
						val a = safeGetDataType(op.args[0], local)
						val b = safeGetDataType(op.args[1], local)
						setDataType(op.args[0] as PTIR.Variable, div(a, b), local)
					}
					PTIR.Op.MULTIPLY -> {
						val a = safeGetDataType(op.args[0], local)
						val b = safeGetDataType(op.args[1], local)
						setDataType(op.args[0] as PTIR.Variable, mul(a, b), local)
					}
					PTIR.Op.ADD -> {
						val a = safeGetDataType(op.args[0], local)
						val b = safeGetDataType(op.args[1], local)
						setDataType(op.args[0] as PTIR.Variable, add(a, b), local)
					}
					PTIR.Op.SUBTRACT -> {
						val a = safeGetDataType(op.args[0], local)
						val b = safeGetDataType(op.args[1], local)
						setDataType(op.args[0] as PTIR.Variable, sub(a, b), local)
					}
					PTIR.Op.AND -> TODO()
					PTIR.Op.OR -> TODO()
					PTIR.Op.SHL -> error("[FATAL][VM] SHL not supported on JVM Interpreter!")
					PTIR.Op.SHR -> {
						val a = safeGetDataType(op.args[0], local)
						val b = safeGetDataType(op.args[1], local)
						setDataType(op.args[0] as PTIR.Variable, shr(a, b), local)
					}
					PTIR.Op.SAR -> {
						val a = safeGetDataType(op.args[0], local)
						val b = safeGetDataType(op.args[1], local)
						setDataType(op.args[0] as PTIR.Variable, sar(a, b), local)
					}
					PTIR.Op.SAL -> {
						val a = safeGetDataType(op.args[0], local)
						val b = safeGetDataType(op.args[1], local)
						setDataType(op.args[0] as PTIR.Variable, sal(a, b), local)
					}
					PTIR.Op.XOR -> {
						val a = safeGetDataType(op.args[0], local)
						val b = safeGetDataType(op.args[1], local)
						setDataType(op.args[0] as PTIR.Variable, xor(a, b), local)
					}
					PTIR.Op.INVOKE -> {
						val a = op.args[0] as PTIR.Variable
						if (args[1] is String) {
							var b = args[1] as String
							if (b.isBlank()) {
								b = name
							}
							val c = args[2] as UInt
							val file = enviornment[b]!!
							val newMethod = file.methods[file.methodIndex[c.toInt()].toInt()]
							if (args.size > 3) {
								setDataType(
									a, invoke(b, newMethod, op.args.subList(3, op.args.size).toTypedArray()), local
								)
							} else {
								setDataType(
									a, invoke(b, newMethod, emptyArray()), local
								)
							}
						} else {
							when (PTIR.STDCall.values[(args[1] as UInt).toInt()]) {
								PTIR.STDCall.PRINT -> print(safeGetDataType(args[2], local))
							}
						}
					}
					PTIR.Op.NULL -> {
						setDataType(op.args[0] as PTIR.Variable, null, local)
					}
					else -> error("[FATAL][VM] Unknown Op: ${op.type}!")
				}
				line++
			}
		} catch (ex: Exception) {
			val debugMessages = if (method.debugInfo.isNotEmpty()) {
				method.debugInfo.filter { debug ->
					debug.methodLinesIndexes.let {
						it[0]..it[1]
					}.contains(line.toUInt())
				}.map { it.methodLinesText }
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