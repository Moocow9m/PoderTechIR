package tech.poder.ir.data.storage.segment

import tech.poder.ir.commands.Command
import tech.poder.ir.commands.DebugValue
import tech.poder.ir.commands.Simple
import tech.poder.ir.commands.SimpleValue
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.Container
import tech.poder.ir.data.base.unlinked.UnlinkedMethod
import tech.poder.ir.util.MemorySegmentBuffer
import java.util.*
import kotlin.math.ceil

@JvmInline
value class SegmentPart(
    val instructions: MutableList<Command> = mutableListOf()
) : Segment {

    companion object {
        private fun safePop(stack: Stack<Type>, message: String): Type {

            check(stack.isNotEmpty()) {
                "$message could not be executed because stack was empty!"
            }

            return stack.pop()
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
    }

    override fun eval(
        dependencies: Set<Container>,
        self: Container,
        method: UnlinkedMethod,
        stack: Stack<Type>,
        currentIndex: Int
    ): Int {
        var index = currentIndex
        var lastDebugLine: CharSequence = ""
        var lastDebugNumber = 0u
        instructions.forEach { command ->
            when (command) {
                Simple.RETURN -> {
                    if (method.returnType != Type.Unit) {
                        val ret = safePop(stack, "RETURN")
                        check(ret == method.returnType) {
                            "Return type error: ${
                                processDebug(
                                    lastDebugNumber,
                                    lastDebugLine
                                )
                            }Wanted: ${method.returnType::class} Got: ${ret::class}"
                        }
                    }
                    check(stack.isEmpty()) {
                        "Stack wasn't empty! ${
                            processDebug(
                                lastDebugNumber,
                                lastDebugLine
                            )
                        }\n\tStack:\n\t\t${stack.joinToString("\n\t\t")}"
                    }
                }
                Simple.POP -> safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}POP")
                Simple.DUP -> stack.push(safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}DUP"))
                Simple.DEC, Simple.INC, Simple.NEG -> {
                    val a = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}${(command as Simple).name}")
                    check(a is Type.Primitive.Numeric) {
                        "${a::class} is not numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    stack.push(a)
                }
                Simple.ADD -> {
                    val b = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ADD")
                    val a = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ADD")
                    check(a is Type.Primitive) {
                        "${a::class} is not numeric or char-based! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    check(b is Type.Primitive) {
                        "${b::class} is not numeric or char-based! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    if (a is Type.Primitive.CharBased || b is Type.Primitive.CharBased) {
                        stack.push(Type.Primitive.CharBased.String)
                    } else {
                        stack.push(toLargerType(a as Type.Primitive.Numeric, b as Type.Primitive.Numeric))
                    }
                }
                Simple.SUB, Simple.MUL, Simple.DIV,
                Simple.XOR, Simple.SAL, Simple.SAR,
                Simple.SHR, Simple.SHL, Simple.AND,
                Simple.OR, Simple.ROL, Simple.ROR
                -> {
                    val b = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}${(command as Simple).name}")
                    val a = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}${command.name}")
                    check(a is Type.Primitive.Numeric) {
                        "${a::class} is not numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    check(b is Type.Primitive.Numeric) {
                        "${b::class} is not numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    stack.push(toLargerType(a, b))
                }
                Simple.ARRAY_SET -> {
                    val value = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ARRAY_SET")
                    val aIndex = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ARRAY_SET")
                    val array = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ARRAY_SET")
                    check(array is Type.Array || array is Type.RuntimeArray) {
                        "${array::class} is not an array! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }

                    check(aIndex is Type.Primitive.Numeric.Basic) {
                        "${aIndex::class} is not a basic numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }

                    if (array is Type.RuntimeArray) {
                        check(value == array.type) {
                            "Array type error: ${value::class} != ${array.type::class} ${
                                processDebug(
                                    lastDebugNumber,
                                    lastDebugLine
                                )
                            }"
                        }
                    } else {
                        array as Type.Array
                        check(value == array.type) {
                            "Array type error: ${value::class} != ${array.type::class} ${
                                processDebug(
                                    lastDebugNumber,
                                    lastDebugLine
                                )
                            }"
                        }
                        check(index < array.size) {
                            "Array size error: $index >= ${array.size} ${processDebug(lastDebugNumber, lastDebugLine)}"
                        }
                    }
                }
                Simple.ARRAY_GET -> {
                    val aIndex = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ARRAY_GET")
                    val array = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ARRAY_GET")
                    check(array is Type.Array || array is Type.RuntimeArray) {
                        "${array::class} is not an array! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }

                    check(aIndex is Type.Primitive.Numeric.Basic) {
                        "${aIndex::class} is not a basic numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }

                    if (array is Type.RuntimeArray) {
                        stack.push(array.type)
                    } else {
                        array as Type.Array
                        stack.push(array.type)
                    }
                }
                is DebugValue.LineNumber -> lastDebugNumber = command.line
                is DebugValue.Line -> lastDebugLine = command.line
                is SimpleValue.PushByte -> {
                    stack.push(Type.Primitive.Numeric.Basic.Byte)
                }
                is SimpleValue.PushShort -> {
                    stack.push(Type.Primitive.Numeric.Basic.Short)
                }
                is SimpleValue.PushInt -> {
                    stack.push(Type.Primitive.Numeric.Basic.Int)
                }
                is SimpleValue.PushLong -> {
                    stack.push(Type.Primitive.Numeric.Basic.Long)
                }
                is SimpleValue.PushFloat -> {
                    stack.push(Type.Primitive.Numeric.FloatingPoint.Float)
                }
                is SimpleValue.PushDouble -> {
                    stack.push(Type.Primitive.Numeric.FloatingPoint.Double)
                }
                is SimpleValue.PushChar -> {
                    stack.push(Type.Primitive.CharBased.Char)
                }
                is SimpleValue.PushChars -> {
                    stack.push(Type.Primitive.CharBased.String)
                }
                is SimpleValue.SystemCall -> {
                    val call = command.data
                    call.args.forEach {
                        val popped = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}SystemCall")
                        check(popped == it) {
                            "SysCall type check error! ${
                                processDebug(
                                    lastDebugNumber,
                                    lastDebugLine
                                )
                            }Wanted: ${it::class} Got: ${popped::class}"
                        }
                    }
                }
                is SimpleValue.IfType -> {
                    val b = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}${command::class}")
                    val a = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}${command::class}")
                    check(a is Type.Primitive.Numeric) {
                        "${a::class} is not numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    check(b is Type.Primitive.Numeric) {
                        "${b::class} is not numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                }
                is SimpleValue.Jump -> {
                    //No stack change
                }
                else -> error("Unrecognized command: ${command::class} ${processDebug(lastDebugNumber, lastDebugLine)}")
            }
            index++
        }

        return index
    }

    private fun processDebug(debugLineNumber: UInt, debugLine: CharSequence): String {
        return if (debugLine.isNotBlank()) {
            "Error at Line: $debugLine:$debugLineNumber "
        } else if (debugLineNumber > 0u) {
            "Error at LineNumber: $debugLineNumber "
        } else {
            ""
        }
    }

    override fun size(): Int {
        return instructions.size
    }

    override fun toBulk(storage: MutableList<Command>) {
        storage.addAll(instructions)
    }

    override fun toBin(buffer: MemorySegmentBuffer) {
        buffer.write(1.toByte())
        buffer.writeVar(instructions.size)
        instructions.forEach {
            it.toBin(buffer)
        }
    }

    override fun sizeBytes(): Long {
        return 1L + MemorySegmentBuffer.varSize(instructions.size) + ceil(instructions.sumOf { it.sizeBits() } / 8.0).toLong()
    }
}
