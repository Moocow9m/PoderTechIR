package tech.poder.ptir

/**
Poder Tech Intermediate Representative
By Moocow9m: 2022
 */

import tech.poder.proto.BitInputStream
import tech.poder.proto.ReadStd
import tech.poder.proto.BitOutputStream
import tech.poder.proto.Packet
import tech.poder.proto.WriteStd

object PTIR {
	enum class STDCall {
		PRINT,
		STREAM,
		;
		companion object {
			val values = values()
			val DEFAULT = PRINT
		}
	}
	enum class Type {
		ARRAY,
		INT,
		FLOAT,
		LIST,
		STRUCT,
		INT8,
		INT16,
		INT32,
		INT64,
		FLOAT32,
		FLOAT64,
		;
		companion object {
			val values = values()
			val DEFAULT = ARRAY
		}
	}
	enum class Op {
		RETURN,
		THROW,
		LOOP,
		BREAK,
		GET_ARRAY_VAR,
		SET_ARRAY_VAR,
		SET_VAR,
		GET_VAR,
		GET_STRUCT_VAR,
		SET_STRUCT_VAR,
		NEW_ARRAY,
		NEW_STRUCT,
		COMPARE,
		IF_NOT_EQUALS,
		IF_EQUALS,
		IF_LESS_THAN,
		IF_GREATER_THAN,
		ELSE,
		REMAINDER,
		DIVIDE,
		MULTIPLY,
		ADD,
		SUBTRACT,
		AND,
		OR,
		SHL,
		SHR,
		SAR,
		SAL,
		XOR,
		INVOKE,
		NULL,
		IF_NULL,
		;
		companion object {
			val values = values()
			val DEFAULT = RETURN
		}
	}
	data class Debug(val methodLinesIndexes: List<UInt> = emptyList(), val methodLinesText: String = "", val breakPoints: Boolean = false): Packet {
		companion object {
			val DEFAULT = Debug()
			fun fromBytes(stream: BitInputStream): Debug {
				val methodLinesIndexes = List(ReadStd.readVUInt(stream).toInt()) {
					val it0 = ReadStd.readVUInt(stream)
					it0
				}
				val methodLinesText = ReadStd.readString(stream)
				val breakPoints = ReadStd.readBoolean(stream)
				return Debug(methodLinesIndexes, methodLinesText, breakPoints)
			}
		}
		override fun toBytes(stream: BitOutputStream) {
			WriteStd.writeVUInt(stream, methodLinesIndexes.size.toUInt())
			methodLinesIndexes.forEach { it0 ->
				WriteStd.writeVUInt(stream, it0)
			}
			WriteStd.writeString(stream, methodLinesText)
			WriteStd.writeBoolean(stream, breakPoints)
		}
	}
	data class Info(val index: List<UInt> = emptyList()): Packet {
		companion object {
			val DEFAULT = Info()
			fun fromBytes(stream: BitInputStream): Info {
				val index = List(ReadStd.readVUInt(stream).toInt()) {
					val it0 = ReadStd.readVUInt(stream)
					it0
				}
				return Info(index)
			}
		}
		override fun toBytes(stream: BitOutputStream) {
			WriteStd.writeVUInt(stream, index.size.toUInt())
			index.forEach { it0 ->
				WriteStd.writeVUInt(stream, it0)
			}
		}
	}
	data class Variable(val local: Boolean = false, val index: UInt = 0u): Packet {
		companion object {
			val DEFAULT = Variable()
			fun fromBytes(stream: BitInputStream): Variable {
				val local = ReadStd.readBoolean(stream)
				val index = ReadStd.readVUInt(stream)
				return Variable(local, index)
			}
		}
		override fun toBytes(stream: BitOutputStream) {
			WriteStd.writeBoolean(stream, local)
			WriteStd.writeVUInt(stream, index)
		}
	}
	data class Expression(val type: Op = Op.DEFAULT, val args: List<Any> = emptyList()): Packet {
		companion object {
			val DEFAULT = Expression()
			fun fromBytes(stream: BitInputStream): Expression {
				val type = Op.values[ReadStd.readVUInt(stream).toInt()]
				val args = List(ReadStd.readVUInt(stream).toInt()) {
					val it0 = mapRead0(stream, ReadStd.readVUInt(stream).toInt())
					it0
				}
				return Expression(type, args)
			}
			private fun mapRead0(stream: BitInputStream, id: Int): Any {
				return when (id) {
					0 -> {
						Op.values[ReadStd.readVUInt(stream).toInt()]
					}
					1 -> {
						Expression.fromBytes(stream)
					}
					2 -> {
						Variable.fromBytes(stream)
					}
					3 -> {
						List(ReadStd.readVUInt(stream).toInt()) {
							mapRead0(stream, ReadStd.readVUInt(stream).toInt())
						}
					}
					4 -> {
						ReadStd.readAny(stream)
					}
					else -> throw java.lang.IllegalArgumentException("Unknown id: $id")
				}
			}
		}
		override fun toBytes(stream: BitOutputStream) {
			WriteStd.writeVUInt(stream, type.ordinal.toUInt())
			WriteStd.writeVUInt(stream, args.size.toUInt())
			args.forEach { it0 ->
				mapWrite0(stream, it0)
			}
		}
		private fun mapWrite0(stream: BitOutputStream, value: Any) {
			return when (value) {
				is Op -> {
					WriteStd.writeVUInt(stream, 0u)
					WriteStd.writeVUInt(stream, value.ordinal.toUInt())
				}
				is Expression -> {
					WriteStd.writeVUInt(stream, 1u)
					value.toBytes(stream)
				}
				is Variable -> {
					WriteStd.writeVUInt(stream, 2u)
					value.toBytes(stream)
				}
				is List<*> -> {
					WriteStd.writeVUInt(stream, 3u)
					WriteStd.writeVUInt(stream, value.size.toUInt())
					value.forEach { it1 ->
						mapWrite0(stream, it1!!)
					}
				}
				is Any -> {
					WriteStd.writeVUInt(stream, 4u)
					WriteStd.writeAny(stream, value)
				}
				else -> throw java.lang.IllegalArgumentException("Unknown type: ${value::class.java.name}")
			}
		}
	}
	data class Method(val bytecode: List<Expression> = emptyList(), val extraInfo: List<Info> = emptyList(), val debugInfo: List<Debug> = emptyList()): Packet {
		companion object {
			val DEFAULT = Method()
			fun fromBytes(stream: BitInputStream): Method {
				val bytecode = List(ReadStd.readVUInt(stream).toInt()) {
					val it0: Expression = ReadStd.readPacket(stream, Expression.Companion)
					it0
				}
				val extraInfo = List(ReadStd.readVUInt(stream).toInt()) {
					val it0: Info = ReadStd.readPacket(stream, Info.Companion)
					it0
				}
				val debugInfo = List(ReadStd.readVUInt(stream).toInt()) {
					val it0: Debug = ReadStd.readPacket(stream, Debug.Companion)
					it0
				}
				return Method(bytecode, extraInfo, debugInfo)
			}
		}
		override fun toBytes(stream: BitOutputStream) {
			WriteStd.writeVUInt(stream, bytecode.size.toUInt())
			bytecode.forEach { it0 ->
				WriteStd.writePacket(stream, it0)
			}
			WriteStd.writeVUInt(stream, extraInfo.size.toUInt())
			extraInfo.forEach { it0 ->
				WriteStd.writePacket(stream, it0)
			}
			WriteStd.writeVUInt(stream, debugInfo.size.toUInt())
			debugInfo.forEach { it0 ->
				WriteStd.writePacket(stream, it0)
			}
		}
	}
	data class FullType(val type: Type = Type.DEFAULT, val unsigned: Boolean = false): Packet {
		companion object {
			val DEFAULT = FullType()
			fun fromBytes(stream: BitInputStream): FullType {
				val type = Type.values[ReadStd.readVUInt(stream).toInt()]
				val unsigned = ReadStd.readBoolean(stream)
				return FullType(type, unsigned)
			}
		}
		override fun toBytes(stream: BitOutputStream) {
			WriteStd.writeVUInt(stream, type.ordinal.toUInt())
			WriteStd.writeBoolean(stream, unsigned)
		}
	}
	data class Code(val id: String = "", val methods: List<Method> = emptyList(), val lastGlobalVarId: UInt = 0u, val structs: List<List<FullType>> = emptyList()): Packet {
		companion object {
			val DEFAULT = Code()
			fun fromBytes(stream: BitInputStream): Code {
				val id = ReadStd.readString(stream)
				val methods = List(ReadStd.readVUInt(stream).toInt()) {
					val it0: Method = ReadStd.readPacket(stream, Method.Companion)
					it0
				}
				val lastGlobalVarId = ReadStd.readVUInt(stream)
				val structs = List(ReadStd.readVUInt(stream).toInt()) {
					val it0 = List(ReadStd.readVUInt(stream).toInt()) {
						val it1: FullType = ReadStd.readPacket(stream, FullType.Companion)
						it1
					}
					it0
				}
				return Code(id, methods, lastGlobalVarId, structs)
			}
		}
		override fun toBytes(stream: BitOutputStream) {
			WriteStd.writeString(stream, id)
			WriteStd.writeVUInt(stream, methods.size.toUInt())
			methods.forEach { it0 ->
				WriteStd.writePacket(stream, it0)
			}
			WriteStd.writeVUInt(stream, lastGlobalVarId)
			WriteStd.writeVUInt(stream, structs.size.toUInt())
			structs.forEach { it0 ->
				WriteStd.writeVUInt(stream, it0.size.toUInt())
				it0.forEach { it1 ->
					WriteStd.writePacket(stream, it1)
				}
			}
		}
	}
}