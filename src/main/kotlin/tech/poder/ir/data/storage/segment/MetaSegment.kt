package tech.poder.ir.data.storage.segment

import tech.poder.ir.commands.Command
import tech.poder.ir.commands.Simple
import tech.poder.ir.commands.SimpleValue
import tech.poder.ir.data.LocationRef
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.api.APIContainer
import tech.poder.ir.data.base.api.PublicMethod
import tech.poder.ir.data.base.unlinked.UnlinkedContainer
import tech.poder.ir.data.base.unlinked.UnlinkedMethod
import tech.poder.ir.metadata.IdMethod
import tech.poder.ir.metadata.NameId
import tech.poder.ir.util.MemorySegmentBuffer
import java.util.*
import kotlin.math.ceil

//complicated flow control....
data class MetaSegment(
	internal val commands: MutableList<Command> = mutableListOf(),
	val loops: List<IntRange> = listOf(),
	val branches: List<Pair<IntRange, IntRange?>> = listOf()
) : Segment {

	fun copy(): MetaSegment {
		return MetaSegment(commands.map { it }.toMutableList(), loops, branches)
	}

	companion object {
		fun buildControlGraph(instructions: List<Command>): MetaSegment {
			val loops = mutableListOf<IntRange>()
			val branches = mutableListOf<Pair<IntRange, IntRange?>>()

			instructions.forEachIndexed { index, command ->
				if (command is SimpleValue.JumpShort) {
					if (command.offset < 0) { //loops must go backwards right?
						loops.add((index + command.offset + 1)..index)
					}
				}
				if (command is SimpleValue.IfTypeShort) {
					if (command.offset() < 0) { //if loop... fun....
						branches.add(
							Pair(
								(index + command.offset() + 1)..index,
								null
							)
						) //should this be treated as a loop or if?
					} else {
						val ifBlock = index..(index + command.offset())
						val last = instructions[ifBlock.last]
						val elseBlock = if (last is SimpleValue.JumpShort && last.offset > 0) {
							(ifBlock.last + 1)..(ifBlock.last + last.offset)
						} else {
							null
						}
						branches.add(Pair(ifBlock, elseBlock))
					}
				}
			}
			return MetaSegment(instructions.toMutableList(), loops, branches)
		}
	}

	override fun eval(
		dependencies: Set<APIContainer>,
		self: UnlinkedContainer,
		method: UnlinkedMethod,
		stack: Stack<Type>,
		currentIndex: Int,
		vars: MutableMap<CharSequence, UInt>,
		type: MutableMap<UInt, Type>,
		depMap: List<NameId>
	): Int {
		val base = Stack<Type>()
		val vars1 = mutableMapOf<String, UInt>()
		val varTypes = mutableMapOf<UInt, Type>()
		method.args.forEach {
			val id = vars1.size.toUInt()
			vars1[it.name] = id
			varTypes[id] = it.type
		}
		processScope(dependencies, self, depMap, base, commands.indices, method.returnType, -1..-1, vars1, varTypes)
		return commands.size //todo return not needed anymore
	}

	private fun processScope(
		dependencies: Set<APIContainer>,
		self: UnlinkedContainer,
		depMap: List<NameId>,
		stack: Stack<Type>,
		scopeRange: IntRange,
		returnType: Type,
		prevScope: IntRange,
		vars: Map<String, UInt>,
		varTypes: Map<UInt, Type>
	): Stack<Type> {
		val copy = Stack<Type>()
		copy.addAll(stack)
		val varCopy = mutableMapOf<String, UInt>()
		varCopy.putAll(vars)
		val varTypeCopy = mutableMapOf<UInt, Type>()
		varTypeCopy.putAll(varTypes)
		var index = scopeRange.first
		while (scopeRange.contains(index)) {
			val command = commands[index]
			val loop = loops.filter { it.first == index && it != prevScope }
			val ifBlocks = branches.filter {
				it.first.first == index && it.first.first..(it.second?.last ?: it.first.last) != prevScope
			}
			if (loop.isNotEmpty() || ifBlocks.isNotEmpty()) {
				var loopSystem = loop.isNotEmpty()
				var largest = 0..0
				loop.forEach {
					if (it.last > largest.last) {
						largest = it
					}
				}
				ifBlocks.forEach {
					if ((it.second?.last ?: it.first.last) > largest.last) {
						largest = it.first.first..(it.second?.last ?: it.first.last)
						loopSystem = false
					}
				}
				if (loopSystem) {
					val check = processScope(
						dependencies,
						self,
						depMap,
						copy,
						largest,
						returnType,
						largest,
						varCopy,
						varTypeCopy
					)
					check(check.size == copy.size) {
						"Stack size mismatch!\n" +
								"\tOriginal:\n" +
								"\t\t${copy.joinToString("\n\t\t")}\n" +
								"\tAfter Loop:\n" +
								"\t\t${check.joinToString("\n\t\t")}"
					}
					check.forEachIndexed { cIndex, type ->
						check(copy[cIndex] == type) {
							"Stack type mismatch! Original: ${copy[cIndex]::class} After Loop: ${type::class}\n" +
									"\tOriginal:\n" +
									"\t\t${copy.joinToString("\n\t\t")}\n" +
									"\tAfter Loop:\n" +
									"\t\t${check.joinToString("\n\t\t")}"
						}
					}
				} else if (ifBlocks.isNotEmpty()) {
					val blocks = ifBlocks.first {
						it.first.first == largest.first && ((it.second?.last ?: it.first.last) == largest.last)
					}
					val b = safePop(copy, "IF_B")
					val a = safePop(copy, "IF_A")
					check(a is Type.Primitive.Numeric) {
						"${a::class} is not numeric!"
					}
					check(b is Type.Primitive.Numeric) {
						"${b::class} is not numeric!"
					}
					val checkIf = processScope(
						dependencies,
						self,
						depMap,
						copy,
						blocks.first,
						returnType,
						largest,
						varCopy,
						varTypeCopy
					)
					val checkElse = if (blocks.second != null) {
						processScope(
							dependencies,
							self,
							depMap,
							copy,
							blocks.second!!,
							returnType,
							largest,
							varCopy,
							varTypeCopy
						)
					} else {
						copy
					}
					check(checkIf.size == checkElse.size) {
						"Stack size mismatch!\n" +
								"\tIf:\n" +
								"\t\t${checkIf.joinToString("\n\t\t")}\n" +
								"\tElse If:\n" +
								"\t\t${checkElse.joinToString("\n\t\t")}"
					}
					checkIf.forEachIndexed { cIndex, type ->
						check(checkElse[cIndex] == type) {
							"Stack type mismatch! If: ${type::class} After Loop: ${checkElse[cIndex]::class}\n" +
									"\tIf:\n" +
									"\t\t${checkIf.joinToString("\n\t\t")}\n" +
									"\tElse If:\n" +
									"\t\t${checkElse.joinToString("\n\t\t")}"
						}
					}

					copy.clear()
					copy.addAll(checkIf)
				}
				if (largest != 0..0) {
					index = largest.last + 1
				}
			} else {
				when (command) {
					Simple.POP -> safePop(copy, "POP")
					Simple.RETURN -> processReturn(copy, returnType)
					Simple.DUP -> copy.push(copy.peek())
					Simple.INC, Simple.DEC, Simple.NEG -> {
						val a = safePop(copy, (command as Simple).name)
						check(a is Type.Primitive.Numeric) {
							"${a::class} is not numeric!"
						}
						copy.push(a)
					}
					Simple.ADD -> {
						val b = safePop(copy, "ADD_B")
						val a = safePop(copy, "ADD_A")
						check(a is Type.Primitive) {
							"${a::class} is not primitive!"
						}
						check(b is Type.Primitive) {
							"${b::class} is not primitive!"
						}
						if (a is Type.Primitive.CharBased || b is Type.Primitive.CharBased) {
							copy.push(Type.Primitive.CharBased.String)
						} else {
							copy.push(toLargerType(a as Type.Primitive.Numeric, b as Type.Primitive.Numeric))
						}
					}
					Simple.SUB, Simple.MUL, Simple.DIV,
					Simple.OR, Simple.AND, Simple.XOR,
					Simple.SAL, Simple.SAR, Simple.SHL,
					Simple.SHR, Simple.ROL, Simple.ROR -> {
						val b = safePop(copy, "${(command as Simple).name}_B")
						val a = safePop(copy, "${command.name}_A")
						check(a is Type.Primitive.Numeric) {
							"${a::class} is not numeric!"
						}
						check(b is Type.Primitive.Numeric) {
							"${b::class} is not numeric!"
						}
						copy.push(toLargerType(a, b))
					}
					Simple.ARRAY_SET -> TODO()
					Simple.ARRAY_GET -> TODO()
					is SimpleValue.SetVar -> {
						val loc = if (command.data is LocationRef.LocationByName) {
							if (varCopy.containsKey(command.data.name)) {
								varCopy[command.data.name]!!
							} else {
								val id = varCopy.size.toUInt()
								commands[index] = SimpleValue.SetVar(LocationRef.LocationByID(id))
								varCopy[command.data.name.toString()] = id
								id
							}
						} else {
							command.data as LocationRef.LocationByID
							command.data.id
						}
						val popped = safePop(copy, "SET_VAR")
						varTypeCopy[loc] = popped
					}
					is SimpleValue.GetVar -> {
						val loc = if (command.data is LocationRef.LocationByName) {
							varCopy[command.data.name]
						} else {
							command.data as LocationRef.LocationByID
							command.data.id
						}
						checkNotNull(loc) {
							"Variable: ${command.data} did not exist!"
						}
						copy.push(varTypeCopy[loc])
					}
					is SimpleValue.PushChars -> copy.push(Type.Primitive.CharBased.String)
					is SimpleValue.PushChar -> copy.push(Type.Primitive.CharBased.Char)
					is SimpleValue.PushInt -> copy.push(Type.Primitive.Numeric.Basic.Int)
					is SimpleValue.PushLong -> copy.push(Type.Primitive.Numeric.Basic.Long)
					is SimpleValue.SystemCall -> {
						command.data.args.forEach {
							val popped = safePop(copy, "SYSCALL")
							check(popped == it) {
								"Type mismatch! Wanted: ${it::class} Got: ${popped::class}"
							}
						}
						if (command.data.return_ != Type.Unit) {
							copy.push(command.data.return_)
						}
					}
					is SimpleValue.Invoke -> {
						val methId = if (command.data is LocationRef.LocationByName) {
							val id = resolveDepMethod(self, dependencies, depMap, command.data.name.toString())
							commands[index] = SimpleValue.Invoke(LocationRef.LocationByCId(Pair(id.cId, id.id)))
							id
						} else {
							command.data as LocationRef.LocationByCId
							if (command.data.id.first == 0u) {
								IdMethod(
									command.data.id.second,
									command.data.id.first,
									self.locateMethod(command.data.id.second)
								)
							} else {
								val name = depMap.first { it.id == command.data.id.first }.name
								IdMethod(
									command.data.id.second,
									command.data.id.first,
									dependencies.first { it.name == name }.locateMethod(command.data.id.second)
								)
							}
						}
						if (methId.method is UnlinkedMethod) {
							methId.method.args.forEach {
								val popped = safePop(copy, "INVOKE")
								check(popped == it.type) {
									"Type mismatch! Wanted: ${it.type::class} Got: ${popped::class}"
								}
							}
							if (methId.method.returnType != Type.Unit) {
								copy.push(methId.method.returnType)
							}
						} else {
							methId.method as PublicMethod
							methId.method.args.forEach {
								val popped = safePop(copy, "INVOKE")
								check(popped == it) {
									"Type mismatch! Wanted: ${it::class} Got: ${popped::class}"
								}
							}
							if (methId.method.returns != Type.Unit) {
								copy.push(methId.method.returns)
							}
						}
					}
					is SimpleValue.JumpShort,
					is SimpleValue.IfTypeShort -> {
					}//no-op
					else -> error("Unknown command: ${command::class}")
				}
				index++
			}
		}

		return copy
	}

	private fun resolveDepMethod(
		thisDep: UnlinkedContainer,
		rest: Set<APIContainer>,
		mapping: List<NameId>,
		target: String
	): IdMethod {
		val container = if (thisDep.getSelfMapping().containsKey(target)) {
			thisDep
		} else {
			val res = rest.firstOrNull { it.locateMethod(target) != null }
			check(res != null) {
				"Could not find method: [$target]"
			}
			res
		}
		val id = container.locateMethod(target)!!
		return IdMethod(id, mapping.first { it.name == container.name }.id, container.locateMethod(id))
	}

	private fun toLargerType(a: Type.Primitive.Numeric, b: Type.Primitive.Numeric): Type {
		return when {
			a is Type.Primitive.Numeric.FloatingPoint.Double || b is Type.Primitive.Numeric.FloatingPoint.Double -> Type.Primitive.Numeric.FloatingPoint.Double
			a is Type.Primitive.Numeric.FloatingPoint.Float || b is Type.Primitive.Numeric.FloatingPoint.Float -> Type.Primitive.Numeric.FloatingPoint.Float
			a is Type.Primitive.Numeric.Basic.Long || b is Type.Primitive.Numeric.Basic.Long -> Type.Primitive.Numeric.Basic.Long
			a is Type.Primitive.Numeric.Basic.Int || b is Type.Primitive.Numeric.Basic.Int -> Type.Primitive.Numeric.Basic.Int
			a is Type.Primitive.Numeric.Basic.Short || b is Type.Primitive.Numeric.Basic.Short -> Type.Primitive.Numeric.Basic.Short
			else -> Type.Primitive.Numeric.Basic.Byte
		}
	}

	private fun safePop(stack: Stack<Type>, message: String): Type {

		check(stack.isNotEmpty()) {
			"$message could not be executed because stack was empty!"
		}

		return stack.pop()
	}

	private fun processReturn(stack: Stack<Type>, returnType: Type) {
		if (returnType != Type.Unit) {
			val popped = safePop(stack, "RETURN")
			check(returnType == popped) {
				"Return type mismatched! Wanted: ${returnType::class} Got: Wanted: ${popped::class}"
			}
		}
		check(stack.isEmpty()) {
			"Stack was not empty at end of method! Stack: \n\t${stack.joinToString("\n\t")}"
		}
	}

	override fun size(): Int {
		return commands.size
	}

	override fun toBulk(storage: MutableList<Command>) {
		storage.addAll(commands)
	}

	override fun toBin(buffer: MemorySegmentBuffer) {
		buffer.writeVar(commands.size)
		commands.forEach {
			it.toBin(buffer)
		}
		buffer.writeVar(loops.size)
		loops.forEach {
			buffer.writeVar(it.first)
			buffer.writeVar(it.last)
		}
		buffer.writeVar(branches.size)
		branches.forEach {
			buffer.writeVar(it.first.first)
			buffer.writeVar(it.first.last)
			if (it.second == null) {
				buffer.write(0.toByte())
			} else {
				buffer.write(1.toByte())
				buffer.writeVar(it.second!!.first)
				buffer.writeVar(it.second!!.last)
			}
		}
	}

	override fun sizeBytes(): Long {
		return MemorySegmentBuffer.varSize(commands.size) + ceil(commands.sumOf { it.sizeBits() } / 8.0).toLong() + MemorySegmentBuffer.varSize(
			loops.size
		) + loops.sumOf { MemorySegmentBuffer.varSize(it.first) + MemorySegmentBuffer.varSize(it.last) } + MemorySegmentBuffer.varSize(
			branches.size
		) + branches.sumOf {
			MemorySegmentBuffer.varSize(it.first.first) + MemorySegmentBuffer.varSize(it.first.last) + 1 + if (it.second == null) {
				0
			} else {
				MemorySegmentBuffer.varSize(
					it.second!!.first
				) + MemorySegmentBuffer.varSize(it.second!!.last)
			}
		}
	}
}
