package tech.poder.ir.vm

import tech.poder.ptir.PTIR
import kotlin.experimental.and
import kotlin.experimental.or
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
		return when (arg) {
			is PTIR.Variable -> {
				if (arg.local) {
					local[arg.index]
				} else {
					global[arg.index]
				}
			}
			is String -> arg
			is Number -> arg
			is UByte -> arg
			is UInt -> arg
			is ULong -> arg
			is UShort -> arg
			is Boolean -> arg
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
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		return when (first) {
			is Byte -> a.toByte() - b.toByte()
			is Short -> a.toShort() - b.toShort()
			is Int -> a.toInt() - b.toInt()
			is Long -> a.toLong() - b.toLong()
			is Float -> a.toFloat() - b.toFloat()
			is Double -> a.toDouble() - b.toDouble()
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
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		return when (first) {
			is Byte -> a.toByte() / b.toByte()
			is Short -> a.toShort() / b.toShort()
			is Int -> a.toInt() / b.toInt()
			is Long -> a.toLong() / b.toLong()
			is Float -> a.toFloat() / b.toFloat()
			is Double -> a.toDouble() / b.toDouble()
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${a::class.java.name}")
		}
	}

	private fun mod(a: Number, b: Number): Number {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		return when (first) {
			is Byte -> a.toByte() % b.toByte()
			is Short -> a.toShort() % b.toShort()
			is Int -> a.toInt() % b.toInt()
			is Long -> a.toLong() % b.toLong()
			is Float -> a.toFloat() % b.toFloat()
			is Double -> a.toDouble() % b.toDouble()
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

	private fun and(a: Number, b: Number): Number {
		return when (a) {
			is Byte -> a and b.toByte()
			is Short -> a and b.toShort()
			is Int -> a and b.toInt()
			is Long -> a and b.toLong()
			is Float, is Double -> throw IllegalStateException("[FATAL][VM] ILLEGAL Shift Type! ${a::class.java.name}")
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${a::class.java.name}")
		}
	}

	private fun or(a: Number, b: Number): Number {
		return when (a) {
			is Byte -> a or b.toByte()
			is Short -> a or b.toShort()
			is Int -> a or b.toInt()
			is Long -> a or b.toLong()
			is Float, is Double -> throw IllegalStateException("[FATAL][VM] ILLEGAL Shift Type! ${a::class.java.name}")
			else -> throw IllegalStateException("[FATAL][VM] Unknown type! ${a::class.java.name}")
		}
	}

	private fun cmp(a: Number, b: Number): Int {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		return when (first) {
			is Byte -> a.toByte().compareTo(b.toByte())
			is Short -> a.toShort().compareTo(b.toShort())
			is Int -> a.toInt().compareTo(b.toInt())
			is Long -> a.toLong().compareTo(b.toLong())
			is Float -> a.toFloat().compareTo(b.toFloat())
			is Double -> a.toDouble().compareTo(b.toDouble())
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
			is String -> a.toString() + b.toString()
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
		return when (first) {
			is String -> TODO("STRING SUB")
			is ULong -> {
				toUnsigned(a) - toUnsigned(b)
			}
			is UInt -> {
				(toUnsigned(a) - toUnsigned(b)).toUInt()
			}
			is UShort -> {
				(toUnsigned(a) - toUnsigned(b)).toUShort()
			}
			is UByte -> {
				(toUnsigned(a) - toUnsigned(b)).toUByte()
			}
			is Number -> sub(a as Number, b as Number)
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
		return when (first) {
			is ULong -> {
				toUnsigned(a) / toUnsigned(b)
			}
			is UInt -> {
				(toUnsigned(a) / toUnsigned(b)).toUInt()
			}
			is UShort -> {
				(toUnsigned(a) / toUnsigned(b)).toUShort()
			}
			is UByte -> {
				(toUnsigned(a) / toUnsigned(b)).toUByte()
			}
			is Number -> div(first, b as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for division! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun mod(a: Any, b: Any): Any {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		return when (first) {
			is ULong -> {
				toUnsigned(a) % toUnsigned(b)
			}
			is UInt -> {
				(toUnsigned(a) % toUnsigned(b)).toUInt()
			}
			is UShort -> {
				(toUnsigned(a) % toUnsigned(b)).toUShort()
			}
			is UByte -> {
				(toUnsigned(a) % toUnsigned(b)).toUByte()
			}
			is Number -> mod(a as Number, b as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for division! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun shr(a: Any, b: Any): Any {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		return when (first) {
			is Number -> shr(a as Number, b as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for SHR! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun sar(a: Any, b: Any): Any {
		return when (a) {
			is ULong -> {
				a shr toSigned(b).toInt()
			}
			is UInt -> {
				a shr toSigned(b).toInt()
			}
			is UShort -> {
				(a.toUInt() shr toSigned(b).toInt()).toUShort()
			}
			is UByte -> {
				(a.toUInt() shr toSigned(b).toInt()).toUByte()
			}
			is Number -> sar(a, b as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for SAR! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun sal(a: Any, b: Any): Any {
		return when (a) {
			is ULong -> {
				a shl toSigned(b).toInt()
			}
			is UInt -> {
				a shl toSigned(b).toInt()
			}
			is UShort -> {
				(a.toUInt() shl toSigned(b).toInt()).toUShort()
			}
			is UByte -> {
				(a.toUInt() shl toSigned(b).toInt()).toUByte()
			}
			is Number -> sal(a, b as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for SAL! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun xor(a: Any, b: Any): Any {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		return when (first) {
			is ULong -> {
				toUnsigned(a) xor toUnsigned(b)
			}
			is UInt -> {
				(toUnsigned(a) xor toUnsigned(b)).toUInt()
			}
			is UShort -> {
				(toUnsigned(a) xor toUnsigned(b)).toUShort()
			}
			is UByte -> {
				(toUnsigned(a) xor toUnsigned(b)).toUByte()
			}
			is Number -> xor(a as Number, b as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for SAL! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun and(a: Any, b: Any): Any {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		return when (first) {
			is ULong -> {
				toUnsigned(a) and toUnsigned(b)
			}
			is UInt -> {
				(toUnsigned(a) and toUnsigned(b)).toUInt()
			}
			is UShort -> {
				(toUnsigned(a) and toUnsigned(b)).toUShort()
			}
			is UByte -> {
				(toUnsigned(a) and toUnsigned(b)).toUByte()
			}
			is Number -> and(a as Number, b as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for SAL! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun or(a: Any, b: Any): Any {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		return when (first) {
			is ULong -> {
				toUnsigned(a) or toUnsigned(b)
			}
			is UInt -> {
				(toUnsigned(a) or toUnsigned(b)).toUInt()
			}
			is UShort -> {
				(toUnsigned(a) or toUnsigned(b)).toUShort()
			}
			is UByte -> {
				(toUnsigned(a) or toUnsigned(b)).toUByte()
			}
			is Number -> or(a as Number, b as Number)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for SAL! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun cmp(a: Any, b: Any): Int {
		val first = if (typeSize(a) >= typeSize(b)) {
			a
		} else {
			b
		}
		return when (first) {
			is String -> a.toString().compareTo(b.toString())
			is ULong -> {
				toUnsigned(a).compareTo(toUnsigned(b))
			}
			is UInt -> {
				toUnsigned(a).compareTo(toUnsigned(b))
			}
			is UShort -> {
				toUnsigned(a).compareTo(toUnsigned(b))
			}
			is UByte -> {
				toUnsigned(a).compareTo(toUnsigned(b))
			}
			is Number -> cmp(a as Number, b as Number)
			is List<*> -> TODO("LIST ADD")
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for addition! (${a::class.java.name} + ${b::class.java.name})")
		}
	}

	private fun readBool(data: Any, local: Map<UInt, Any>): Boolean {
		return when (data) {
			is Number -> data != 0
			is Boolean -> data
			is ULong -> data != 0u
			is UInt -> data != 0u
			is UShort -> data != 0u
			is UByte -> data != 0u
			is PTIR.Variable -> readBool(safeGetDataType(data, local), local)
			else -> throw IllegalStateException("[FATAL][VM] Invalid types for Bool! ${data::class.java.name}")
		}
	}

	private fun invoke(name: String, method: PTIR.Method, args: Array<out Any>): Any? {
		val local = mutableMapOf<UInt, Any>()
		local[0u] = mutableListOf<Any?>(*args)
		var line = 0
		try {
			val inside = mutableListOf<Boolean>()
			val loops = mutableListOf<LoopState>()
			val ifs = mutableListOf<IfState>()
			while (line < method.bytecode.size) {
				val op = method.bytecode[line]
				when (op.type) {
					PTIR.Op.RETURN -> {
						return if (op.args.size > 1) {
							safeGetDataType(op.args[1], local)
						} else {
							null
						}
					}
					PTIR.Op.THROW -> throw IllegalStateException("[FATAL][IR]" + op.args[0].toString()) //TODO caching
					PTIR.Op.LOOP -> {
						val condition = op.args[0] as PTIR.Variable
						val start = op.args[1] as UInt
						val end = op.args[2] as UInt
						if (readBool(condition, local)) {
							loops.add(LoopState(condition, start.toInt(), end.toInt()))
							inside.add(true)
						} else {
							line = end.toInt()
						}
					}
					PTIR.Op.BREAK -> {
						if (inside.removeLast()) {
							val loop = loops.removeLast()
							line = loop.end
						} else {
							val loop = ifs.removeLast()
							line = loop.afterElse
						}
					}
					PTIR.Op.GET_ARRAY_VAR -> {
						val set = op.args[0] as PTIR.Variable
						val get = safeGetDataType(op.args[1], local) as List<*>
						val index = (op.args[2] as UInt).toInt()
						check(get.size > index) {
							"[FATAL][IR] Index out of bounds!"
						}
						setDataType(set, get[index], local)
					}
					PTIR.Op.SET_ARRAY_VAR -> {
						val set = safeGetDataType(op.args[0] as PTIR.Variable, local) as MutableList<Any?> //use map instead?
						val get = safeGetDataType(op.args[1], local)
						val index = (op.args[2] as UInt).toInt()
						while (set.size < index) {
							set.add(null)
						}
						set[index] = get
					}
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
					PTIR.Op.NEW_ARRAY -> {
						val set = op.args[0] as PTIR.Variable
						setDataType(set, mutableListOf<Any?>(), local)
					}
					PTIR.Op.NEW_STRUCT -> TODO()
					PTIR.Op.COMPARE -> {
						val a = safeGetDataType(op.args[1], local)
						val b = safeGetDataType(op.args[2], local)
						setDataType(op.args[0] as PTIR.Variable, cmp(a, b), local)
					}
					PTIR.Op.IF_NOT_EQUALS -> {
						val a = safeGetDataType(op.args[0], local)
						val b = safeGetDataType(op.args[1], local)
						val result = cmp(a, b)
						if (result == 0) {
							line = (op.args[2] as UInt).toInt()
						} else {
							ifs.add(IfState((op.args[2] as UInt).toInt(), (op.args[3] as UInt).toInt()))
							inside.add(false)
						}
					}
					PTIR.Op.IF_EQUALS -> {
						val a = safeGetDataType(op.args[0], local)
						val b = safeGetDataType(op.args[1], local)
						val result = cmp(a, b)
						if (result == 0) {
							ifs.add(IfState((op.args[2] as UInt).toInt(), (op.args[3] as UInt).toInt()))
							inside.add(false)
						} else {
							line = (op.args[2] as UInt).toInt()
						}
					}
					PTIR.Op.IF_LESS_THAN -> {
						val a = safeGetDataType(op.args[0], local)
						val b = safeGetDataType(op.args[1], local)
						val result = cmp(a, b)
						if (result < 0) {
							ifs.add(IfState((op.args[2] as UInt).toInt(), (op.args[3] as UInt).toInt()))
							inside.add(false)
						} else {
							line = (op.args[2] as UInt).toInt()
						}
					}
					PTIR.Op.IF_GREATER_THAN -> {
						val a = safeGetDataType(op.args[0], local)
						val b = safeGetDataType(op.args[1], local)
						val result = cmp(a, b)
						if (result > 0) {
							ifs.add(IfState((op.args[2] as UInt).toInt(), (op.args[3] as UInt).toInt()))
							inside.add(false)
						} else {
							line = (op.args[2] as UInt).toInt()
						}
					}
					PTIR.Op.IF_NULL -> {
						val a = getDataType(op.args[0], local)
						if (a == null) {
							ifs.add(IfState((op.args[2] as UInt).toInt(), (op.args[3] as UInt).toInt()))
							inside.add(false)
						} else {
							line = (op.args[2] as UInt).toInt()
						}
					}
					PTIR.Op.ELSE -> {
						/*no op*/
					}
					PTIR.Op.REMAINDER -> {
						val a = safeGetDataType(op.args[1], local)
						val b = safeGetDataType(op.args[2], local)
						setDataType(op.args[0] as PTIR.Variable, mod(a, b), local)
					}
					PTIR.Op.DIVIDE -> {
						val a = safeGetDataType(op.args[1], local)
						val b = safeGetDataType(op.args[2], local)
						setDataType(op.args[0] as PTIR.Variable, div(a, b), local)
					}
					PTIR.Op.MULTIPLY -> {
						val a = safeGetDataType(op.args[1], local)
						val b = safeGetDataType(op.args[2], local)
						setDataType(op.args[0] as PTIR.Variable, mul(a, b), local)
					}
					PTIR.Op.ADD -> {
						val a = safeGetDataType(op.args[1], local)
						val b = safeGetDataType(op.args[2], local)
						setDataType(op.args[0] as PTIR.Variable, add(a, b), local)
					}
					PTIR.Op.SUBTRACT -> {
						val a = safeGetDataType(op.args[1], local)
						val b = safeGetDataType(op.args[2], local)
						setDataType(op.args[0] as PTIR.Variable, sub(a, b), local)
					}
					PTIR.Op.AND -> {
						val a = safeGetDataType(op.args[1], local)
						val b = safeGetDataType(op.args[2], local)
						setDataType(op.args[0] as PTIR.Variable, and(a, b), local)
					}
					PTIR.Op.OR -> {
						val a = safeGetDataType(op.args[1], local)
						val b = safeGetDataType(op.args[2], local)
						setDataType(op.args[0] as PTIR.Variable, or(a, b), local)
					}
					PTIR.Op.SHL -> error("[FATAL][VM] SHL not supported on JVM Interpreter!")
					PTIR.Op.SHR -> {
						val a = safeGetDataType(op.args[1], local)
						val b = safeGetDataType(op.args[2], local)
						setDataType(op.args[0] as PTIR.Variable, shr(a, b), local)
					}
					PTIR.Op.SAR -> {
						val a = safeGetDataType(op.args[1], local)
						val b = safeGetDataType(op.args[2], local)
						setDataType(op.args[0] as PTIR.Variable, sar(a, b), local)
					}
					PTIR.Op.SAL -> {
						val a = safeGetDataType(op.args[1], local)
						val b = safeGetDataType(op.args[2], local)
						setDataType(op.args[0] as PTIR.Variable, sal(a, b), local)
					}
					PTIR.Op.XOR -> {
						val a = safeGetDataType(op.args[1], local)
						val b = safeGetDataType(op.args[2], local)
						setDataType(op.args[0] as PTIR.Variable, xor(a, b), local)
					}
					PTIR.Op.INVOKE -> {
						val a = op.args[0] as PTIR.Variable
						if (op.args[1] is String) {
							var b = op.args[1] as String
							if (b.isBlank()) {
								b = name
							}
							val c = op.args[2] as UInt
							val file = enviornment[b]!!
							val newMethod = file.methods[file.methodIndex[c.toInt()].toInt()]
							if (op.args.size > 3) {
								setDataType(
									a, invoke(b, newMethod, op.args.subList(3, op.args.size).toTypedArray()), local
								)
							} else {
								setDataType(
									a, invoke(b, newMethod, emptyArray()), local
								)
							}
						} else {
							when (PTIR.STDCall.values[(op.args[1] as UInt).toInt()]) {
								PTIR.STDCall.PRINT -> print(safeGetDataType(op.args[2], local))
								else -> error("[FATAL][VM] Unknown STDCALL: ${PTIR.STDCall.values[(op.args[1] as UInt).toInt()]}!")
							}
						}
					}
					PTIR.Op.NULL -> {
						setDataType(op.args[0] as PTIR.Variable, null, local)
					}
					else -> error("[FATAL][VM] Unknown Op: ${op.type}!")
				}
				if (inside.isEmpty()) {
					line++
				} else if (inside.last()) {
					val loop = loops.last()
					if (line == loop.end) {
						if (readBool(loop.condition, local)) {
							line = loop.start
						} else {
							loops.removeLast()
							inside.removeLast()
							line++
						}
					} else {
						line++
					}
				} else {
					val if_ = ifs.last()
					if (if_.endIf == line) {
						inside.removeLast()
						ifs.removeLast()
						line = if_.afterElse
					} else {
						line++
					}
				}
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
			println("Error on line $line of $name.ptir [${ex.message}]")
			if (debugMessages.isNotEmpty()) {
				println("\tDebug messages:")
				debugMessages.forEach { println("\t\t$it") }
			}
			throw ex
		}
		return null
	}
}