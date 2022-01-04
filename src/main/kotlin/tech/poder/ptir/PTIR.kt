package tech.poder.ptir

/**
Poder Tech Intermediate Representative
By Moocow9m: 2022
 */

import tech.poder.proto.Packet
import tech.poder.proto.ReadStd
import tech.poder.proto.WriteStd

object PTIR {
	enum class Op {
		GET_VAR,
		SET_VAR,
		RETURN,
		THROW,
		JUMP,
		COMPARE,
		NOT,
		IF_EQUALS,
		IF_LESS_THAN,
		IF_GREATER_THAN,
		REMAINDER,
		DIVIDE,
		MULTIPLY,
		ADD,
		SUBTRACT,
		INVOKE,
		NULL,
		IF_NULL,
		;

		companion object {
			val values = values()
			val DEFAULT = GET_VAR
		}
	}

	data class Debug(
		val methodLinesIndexes: List<UInt> = emptyList(),
		val methodLinesText: String = "",
		val breakPoints: Boolean = false
	) : Packet {
		companion object {
			val DEFAULT = Debug()
			fun fromBytes(stream: java.io.InputStream): Debug {
				val methodLinesIndexes = List(ReadStd.readVUInt(stream).toInt()) {
					val it0 = ReadStd.readVUInt(stream)
					it0
				}
				val methodLinesText = ReadStd.readString(stream)
				val breakPoints = stream.read() != 0
				return Debug(methodLinesIndexes, methodLinesText, breakPoints)
			}
		}

		override fun toBytes(stream: java.io.OutputStream) {
			WriteStd.writeVUInt(stream, methodLinesIndexes.size.toUInt())
			methodLinesIndexes.forEach { it0 ->
				WriteStd.writeVUInt(stream, it0)
			}
			WriteStd.writeString(stream, methodLinesText)
			if (breakPoints) {
				WriteStd.writeVUInt(stream, 1u)
			} else {
				WriteStd.writeVUInt(stream, 0u)
			}
		}
	}

	data class Info(val index: List<UInt> = emptyList()) : Packet {
		companion object {
			val DEFAULT = Info()
			fun fromBytes(stream: java.io.InputStream): Info {
				val index = List(ReadStd.readVUInt(stream).toInt()) {
					val it0 = ReadStd.readVUInt(stream)
					it0
				}
				return Info(index)
			}
		}

		override fun toBytes(stream: java.io.OutputStream) {
			WriteStd.writeVUInt(stream, index.size.toUInt())
			index.forEach { it0 ->
				WriteStd.writeVUInt(stream, it0)
			}
		}
	}

	data class Expression(val type: Op = Op.DEFAULT, val args: List<Any> = emptyList()) : Packet {
		companion object {
			val DEFAULT = Expression()
			fun fromBytes(stream: java.io.InputStream): Expression {
				val type = Op.values[ReadStd.readVUInt(stream).toInt()]
				val args = List(ReadStd.readVUInt(stream).toInt()) {
					val it0 = mapRead0(stream, ReadStd.readVUInt(stream).toInt())
					it0
				}
				return Expression(type, args)
			}

			private fun mapRead0(stream: java.io.InputStream, id: Int): Any {
				return when (id) {
					0 -> {
						ReadStd.readVInt(stream)
					}
					1 -> {
						ReadStd.readVUInt(stream)
					}
					2 -> {
						ReadStd.readString(stream)
					}
					3 -> {
						Op.values[ReadStd.readVUInt(stream).toInt()]
					}
					4 -> {
						Expression.fromBytes(stream)
					}
					5 -> {
						List(ReadStd.readVUInt(stream).toInt()) {
							mapRead0(stream, ReadStd.readVUInt(stream).toInt())
						}
					}
					else -> throw java.lang.IllegalArgumentException("Unknown id: $id")
				}
			}
		}

		override fun toBytes(stream: java.io.OutputStream) {
			WriteStd.writeVUInt(stream, type.ordinal.toUInt())
			WriteStd.writeVUInt(stream, args.size.toUInt())
			args.forEach { it0 ->
				mapWrite0(stream, it0)
			}
		}

		private fun mapWrite0(stream: java.io.OutputStream, value: Any) {
			return when (value) {
				is Int -> {
					WriteStd.writeVUInt(stream, 0u)
					WriteStd.writeVInt(stream, value)
				}
				is UInt -> {
					WriteStd.writeVUInt(stream, 1u)
					WriteStd.writeVUInt(stream, value)
				}
				is String -> {
					WriteStd.writeVUInt(stream, 2u)
					WriteStd.writeString(stream, value)
				}
				is Op -> {
					WriteStd.writeVUInt(stream, 3u)
					WriteStd.writeVUInt(stream, value.ordinal.toUInt())
				}
				is Expression -> {
					WriteStd.writeVUInt(stream, 4u)
					value.toBytes(stream)
				}
				is List<*> -> {
					WriteStd.writeVUInt(stream, 5u)
					value.forEach { it1 ->
						mapWrite0(stream, it1!!)
					}
				}
				else -> throw java.lang.IllegalArgumentException("Unknown type: ${value::class.simpleName}")
			}
		}
	}

	data class Method(
		val bytecode: List<Expression> = emptyList(),
		val extraInfo: List<Info> = emptyList(),
		val debugInfo: List<Debug> = emptyList()
	) : Packet {
		companion object {
			val DEFAULT = Method()
			fun fromBytes(stream: java.io.InputStream): Method {
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

		override fun toBytes(stream: java.io.OutputStream) {
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

	data class Code(val id: String = "", val methods: List<Method> = emptyList()) : Packet {
		companion object {
			val DEFAULT = Code()
			fun fromBytes(stream: java.io.InputStream): Code {
				val id = ReadStd.readString(stream)
				val methods = List(ReadStd.readVUInt(stream).toInt()) {
					val it0: Method = ReadStd.readPacket(stream, Method.Companion)
					it0
				}
				return Code(id, methods)
			}
		}

		override fun toBytes(stream: java.io.OutputStream) {
			WriteStd.writeString(stream, id)
			WriteStd.writeVUInt(stream, methods.size.toUInt())
			methods.forEach { it0 ->
				WriteStd.writePacket(stream, it0)
			}
		}
	}
}