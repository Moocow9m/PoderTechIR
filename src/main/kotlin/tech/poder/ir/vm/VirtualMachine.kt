package tech.poder.ir.vm

//import java.net.http.HttpClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tech.poder.ir.api.Variable
import tech.poder.ptir.PTIR
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

object VirtualMachine {
	private val environment = mutableMapOf<String, PTIR.Code>()
	private val headFrag = ConcurrentLinkedQueue<UInt>()
	private val last = AtomicInteger(2)
	private val heap = mutableMapOf<UInt, Any>()
	private val codeInit = mutableSetOf<String>()
	private val emptyList = 1u

	private fun allocate(): UInt {
		val head = headFrag.poll()
		if (head != null) {
			return head
		}
		return last.getAndIncrement().toUInt()
	}

	private fun deallocate(address: UInt) {
		heap.remove(address)
		headFrag.add(address)
	}

	fun resetEnv() {
		heap.clear()
		headFrag.clear()
		last.set(1)
		heap[emptyList] = emptyList<Any>()
		environment.clear()
		codeInit.clear()
	}

	fun loadFile(code: PTIR.Code) {
		environment[code.id] = code
	}

	fun exec(code: PTIR.Code, method: UInt, vararg args: Any) {
		loadFile(code)
		runBlocking {
			if (args.isEmpty()) {
				invoke(code.id, method.toInt(), emptyList)
			} else {
				val id = allocate()
				heap[id] = args.toMutableList()
				invoke(code.id, method.toInt(), id)
				deallocate(id)
			}
		}
	}

	private fun getDataType(arg: Any, local: Map<UInt, UInt>, localOnly: Boolean = false): Any? {
		return when (arg) {
			is PTIR.Variable -> {
				if (arg.local) {
					val loc = local[arg.index]
					if (loc == null) {
						null
					} else {
						heap[loc]
					}
				} else {
					if (localOnly) {
						arg
					} else {
						heap[arg.index]
					}
				}
			}
			is String -> arg
			is Number -> arg
			is UByte -> arg
			is UInt -> arg
			is ULong -> arg
			is UShort -> arg
			is Boolean -> arg
			is Job -> arg
			is Collection<*> -> arg.toMutableList()
			else -> TODO(arg::class.java.name)
		}
	}

	private fun safeGetDataType(arg: Any, local: Map<UInt, UInt>, localOnly: Boolean = false): Any {
		val tmp = getDataType(arg, local, localOnly)
		check(tmp != null) {
			"[FATAL][VM] Variable was null on GET!"
		}
		return tmp
	}

	private fun setDataType(variable: PTIR.Variable, value: Any?, local: MutableMap<UInt, UInt>) {
		if (value == null) {
			if (variable.local) {
				local.remove(variable.index)
			} else if (variable.index != 0u) { //Don't GC the empty list
				deallocate(variable.index)
			}
		} else {
			val tmp = safeGetDataType(value, local)
			setDataType(variable, null, local) //Garbage collection (GC)
			val id = if (variable.local) {
				val id = allocate()
				local[variable.index] = id
				id
			} else {
				allocate()
			}
			heap[id] = tmp
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
			is Number -> add(first, toSigned(second))
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

	private fun readBool(data: Any, local: Map<UInt, UInt>): Boolean {
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

	private fun defaultType(type: PTIR.FullType): Any? {
		return when (type.type) {
			PTIR.Type.INT, PTIR.Type.INT64 -> if (type.unsigned) {
				0uL
			} else {
				0L
			}
			PTIR.Type.FLOAT, PTIR.Type.FLOAT64 -> 0.0
			PTIR.Type.ARRAY, PTIR.Type.LIST -> mutableListOf<Any?>()
			PTIR.Type.INT8 -> if (type.unsigned) {
				0.toUByte()
			} else {
				0.toByte()
			}
			PTIR.Type.INT16 -> if (type.unsigned) {
				0.toUShort()
			} else {
				0.toShort()
			}
			PTIR.Type.INT32 -> if (type.unsigned) {
				0
			} else {
				0u
			}
			PTIR.Type.FLOAT32 -> 0f
			PTIR.Type.STRUCT -> null
			else -> error("[FATAL][VM] Invalid types for Default System! $type")
		}
	}

	private suspend fun invoke(name: String, methodId: Int, args: UInt): Any? = runBlocking {
		if (methodId != 0 && !codeInit.contains(name)) {
			invoke(name, 0, args)
		}
		val method = environment[name]!!.methods[methodId]
		val local = mutableMapOf<UInt, UInt>()
		local[0u] = args
		var line = 0
		try {
			val inside = mutableListOf<Boolean>()
			val loops = mutableListOf<LoopState>()
			val ifs = mutableListOf<IfState>()
			while (line < method.bytecode.size) {
				val op = method.bytecode[line]
				when (op.type) {
					PTIR.Op.RETURN -> {
						return@runBlocking if (op.args.size > 1) {
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
						val index = toSigned(safeGetDataType(op.args[2], local)).toInt()
						check(get.size > index) {
							"[FATAL][IR] Index out of bounds!"
						}
						setDataType(set, get[index], local)
					}
					PTIR.Op.SET_ARRAY_VAR -> {
						val set =
							safeGetDataType(op.args[0] as PTIR.Variable, local) as MutableList<Any?> //use map instead?
						val get = safeGetDataType(op.args[1], local)
						val index = toSigned(safeGetDataType(op.args[2], local)).toInt()
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
					PTIR.Op.GET_STRUCT_VAR -> {
						val set = op.args[0] as PTIR.Variable
						val structVar = safeGetDataType(op.args[1] as PTIR.Variable, local) as List<Any?>
						val field = op.args[2] as UInt
						setDataType(set, structVar[field.toInt()], local)
					}
					PTIR.Op.SET_STRUCT_VAR -> {
						val setStruct = safeGetDataType(op.args[0] as PTIR.Variable, local) as MutableList<Any?>
						val get = safeGetDataType(op.args[1] as PTIR.Variable, local)
						val field = op.args[2] as UInt
						setStruct[field.toInt()] = get
					}
					PTIR.Op.NEW_ARRAY -> {
						val set = op.args[0] as PTIR.Variable
						setDataType(set, mutableListOf<Any?>(), local)
					}
					PTIR.Op.NEW_STRUCT -> {
						val store = op.args[0] as PTIR.Variable
						val allStructs = environment[name]!!.structs
						val struct = allStructs[(op.args[1] as UInt).toInt()]
						val newStruct = MutableList(struct.size) {
							defaultType(struct[it])
						}
						setDataType(store, newStruct, local)
					}
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
					PTIR.Op.TYPE_OF -> {
						val res = when (val type = safeGetDataType(op.args[1], local)) {
							is UInt -> {
								0u
							}
							is Int -> {
								1u
							}
							is Byte -> {
								3u
							}
							is UByte -> {
								4u
							}
							is Long -> {
								5u
							}
							is ULong -> {
								6u
							}
							is Short -> {
								7u
							}
							is UShort -> {
								8u
							}
							is List<*> -> { //May be Struct or Normal List!
								9u
							}
							else -> error("Type not recognized: ${type::class.java.name}")
						}
						setDataType(op.args[0] as PTIR.Variable, res, local)
					}
					PTIR.Op.LAUNCH -> {
						val a = op.args[0]
						val returnTo = if (a is PTIR.Variable) {
							a
						} else {
							Variable.VOID
						}

						val invokePointer = if (returnTo == Variable.VOID) {
							1
						} else {
							0
						}
						var b = op.args[invokePointer] as String
						if (b.isBlank()) {
							b = name
						}
						val c = op.args[invokePointer + 1] as UInt

						val job = if (op.args.size > invokePointer + 1) {
							launch {
								val argIndex = allocate()
								heap[argIndex] =
									op.args.subList(invokePointer + 2, op.args.size).map { safeGetDataType(it, local, true) }
										.toMutableList()
								setDataType(
									Variable.VOID,
									invoke(
										b,
										c.toInt(),
										argIndex
									),
									local
								)
								deallocate(argIndex)
							}
						} else {
							launch {
								setDataType(
									Variable.VOID, invoke(b, c.toInt(), emptyList), local
								)
							}
						}
						setDataType(returnTo, job, local)
					}
					PTIR.Op.AWAIT -> {
						val a = safeGetDataType(op.args[0], local) as Job
						a.join()
					}
					PTIR.Op.INVOKE -> {
						val a = op.args[0] as PTIR.Variable
						if (op.args[1] is String) {
							var b = op.args[1] as String
							if (b.isBlank()) {
								b = name
							}
							val c = op.args[2] as UInt
							if (op.args.size > 3) {
								val argIndex = allocate()
								heap[argIndex] =
									op.args.subList(3, op.args.size).map { safeGetDataType(it, local, true) }
										.toMutableList()
								setDataType(
									a,
									invoke(
										b,
										c.toInt(),
										argIndex
									),
									local
								)
								deallocate(argIndex)
							} else {
								setDataType(
									a, invoke(b, c.toInt(), emptyList), local
								)
							}
						} else {
							when (PTIR.STDCall.values[(op.args[1] as UInt).toInt()]) {
								PTIR.STDCall.PRINT -> print(safeGetDataType(op.args[2], local))
								PTIR.STDCall.STREAM -> when (op.args[2] as UInt) {
									0u -> {
										val location = op.args[3] as String
										val protocolTarget = location.split("://")
										when (protocolTarget[0].lowercase()) {
											"http", "https" -> {
												TODO() //may not do till after bootstrap due to rewrite complexity
											}
											"file" -> {
												setDataType(
													a,
													Files.newByteChannel(
														Paths.get(protocolTarget[1]).toAbsolutePath(),
														StandardOpenOption.CREATE,
														StandardOpenOption.READ,
														StandardOpenOption.WRITE
													),
													local
												)
											}
										}
									}
									1u -> {
										when (val res = safeGetDataType(a, local)) {
											is SeekableByteChannel -> {
												res.close()
											}
											else -> TODO()
										}
									}
									2u -> {
										val location = op.args[3] as Variable
										when (val obj = safeGetDataType(location, local)) {
											is SeekableByteChannel -> {
												var res = getDataType(a, local)
												if (res == null) {
													setDataType(a, ByteBuffer.allocate(1024), local)
													res = getDataType(a, local)
												} else {
													when (res) {
														is List<*> -> {
															setDataType(
																a,
																ByteBuffer.wrap(res.map { toSigned(it!!).toByte() }
																	.toByteArray()),
																local
															)
															res = getDataType(a, local)
														}
														is ByteBuffer -> {
															//no op
														}
														else -> TODO(res.toString())
													}
												}
												res as ByteBuffer
												obj.read(res)
												res.flip()
											}
											else -> TODO(obj.toString())
										}
									}
									3u -> {
										val location = op.args[3] as Variable
										when (val obj = safeGetDataType(location, local)) {
											is SeekableByteChannel -> {
												var res = getDataType(a, local)
												if (res == null) {
													setDataType(a, ByteBuffer.allocate(1024), local)
													res = getDataType(a, local)
												} else {
													when (res) {
														is List<*> -> {
															setDataType(
																a,
																ByteBuffer.wrap(res.map { toSigned(it!!).toByte() }
																	.toByteArray()),
																local
															)
															res = getDataType(a, local)
														}
														is ByteBuffer -> {
															//no op
														}
														else -> TODO(res.toString())
													}
												}
												res as ByteBuffer
												obj.write(res)
												res.clear()
											}
											else -> TODO(obj.toString())
										}
									}
									4u -> {
										val location = op.args[3] as Variable
										val size = when (val res = safeGetDataType(location, local)) {
											is SeekableByteChannel -> {
												res.size()
											}
											is ByteBuffer -> {
												res.limit()
											}
											else -> TODO()
										}
										setDataType(a, size, local)
									}
									5u -> {
										val location = op.args[3] as Variable
										val size = when (val res = safeGetDataType(location, local)) {
											is SeekableByteChannel -> {
												res.position()
											}
											is ByteBuffer -> {
												res.position()
											}
											else -> TODO()
										}
										setDataType(a, size, local)
									}
									6u -> {
										val obj = safeGetDataType(op.args[3], local)
										when (val res = safeGetDataType(a, local)) {
											is SeekableByteChannel -> {
												res.position(toSigned(obj).toLong())
											}
											is ByteBuffer -> {
												res.position(toSigned(obj).toInt())
											}
											else -> TODO()
										}
										//set position(file only)
										//submit(http only)
									}
									7u -> {
										val res = when (val obj = safeGetDataType(op.args[3], local)) {
											is SeekableByteChannel -> {
												val buf = ByteBuffer.allocate(1024)
												obj.read(buf)
												buf.flip()
												List(buf.remaining()) {
													buf.get()
												}
											}
											is ByteBuffer -> {
												List(obj.remaining()) {
													obj.get()
												}
											}
											else -> TODO(obj.toString())
										}
										setDataType(a, res, local)
									}
								}
								PTIR.STDCall.STRING_UTILS -> when (op.args[2] as UInt) {
									0u -> {
										val location = safeGetDataType(op.args[3], local) as String
										setDataType(a, location.encodeToByteArray().toMutableList(), local)
										//stringToBytes
									}
									1u -> {
										val location = safeGetDataType(op.args[3], local) as List<Byte>
										setDataType(a, location.toByteArray().decodeToString(), local)
										//bytesToString
									}
								}
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
		null
	}
}