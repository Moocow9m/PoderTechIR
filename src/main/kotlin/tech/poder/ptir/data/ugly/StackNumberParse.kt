package tech.poder.ptir.data.ugly

import tech.poder.ptir.commands.Simple
import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Label
import tech.poder.ptir.data.storage.Type
import tech.poder.ptir.data.storage.segment.SegmentPart.Companion.deleteInstruction
import tech.poder.ptir.data.storage.segment.SegmentPart.Companion.safePop
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

object StackNumberParse {
    internal fun parse(
        cIndex: Int,
        oIndex: Int,
        instruction: Instruction,
        stack: Stack<Type>,
        instructions: ArrayList<Instruction>,
        labels: MutableMap<Int, Label>
    ): Int {
        var index = cIndex
        when (instruction.opCode) {
            Simple.INC -> {
                val popped = safePop(stack, "INC")
                check(popped is Type.Constant && popped !is Type.Constant.TString) {
                    "INC called on illegal type: $popped!"
                }
                if (popped.constant) {
                    val prev = instructions[index - 1]
                    prev.extra = addNumbers(prev.extra as Number, toSameNumbers(1, prev.extra as Number))
                    deleteInstruction(oIndex, index, labels, instructions)
                    index--
                } else {
                    popped.constant = false
                }
                stack.push(popped)
            }
            Simple.NEG -> {
                val popped = safePop(stack, "NEG")
                check(popped is Type.Constant && popped !is Type.Constant.TString) {
                    "DEC called on illegal type: $popped!"
                }
                if (popped.constant) {
                    val prev = instructions[index - 1]
                    prev.extra = negNumber(prev.extra as Number)
                    deleteInstruction(oIndex, index, labels, instructions)
                    index--
                } else {
                    popped.constant = false
                }
                stack.push(popped)
            }
            Simple.DEC -> {
                val popped = safePop(stack, "DEC")
                check(popped is Type.Constant && popped !is Type.Constant.TString) {
                    "DEC called on illegal type: $popped!"
                }
                if (popped.constant) {
                    val prev = instructions[index - 1]
                    prev.extra = subNumbers(prev.extra as Number, toSameNumbers(1, prev.extra as Number))
                    deleteInstruction(oIndex, index, labels, instructions)
                    index--
                } else {
                    popped.constant = false
                }
                stack.push(popped)
            }
            Simple.SUB -> {
                val poppedB = safePop(stack, "SUB1")
                check(poppedB is Type.Constant && poppedB !is Type.Constant.TString) {
                    "SUB called on illegal type: $poppedB!"
                }
                val poppedA = safePop(stack, "SUB2")
                check(poppedA is Type.Constant && poppedA !is Type.Constant.TString) {
                    "SUB called on illegal type: $poppedA!"
                }
                val prevB = instructions[index - 1]
                val prevA = instructions[index - 2]
                stack.push(
                    if (poppedB.constant && poppedA.constant) {
                        prevA.extra = subNumbers(prevA.extra as Number, prevB.extra as Number)
                        deleteInstruction(oIndex, index, labels, instructions)
                        deleteInstruction(oIndex, index - 1, labels, instructions)
                        index -= 2
                        val tmp = toLarger(poppedA, poppedB)
                        tmp.constant = true
                        tmp
                    } else {
                        if (poppedB.constant && (prevB.extra as Number).toDouble() == 1.0) {
                            deleteInstruction(oIndex, index - 1, labels, instructions)
                            instruction.opCode = Simple.DEC
                        }
                        if (poppedA.constant && (prevA.extra as Number).toDouble() == 1.0) {
                            deleteInstruction(oIndex, index - 2, labels, instructions)
                            instruction.opCode = Simple.DEC
                        }
                        toLarger(poppedA, poppedB)
                    }
                )
            }
            Simple.MUL -> {
                val poppedB = safePop(stack, "MUL1")
                check(poppedB is Type.Constant && poppedB !is Type.Constant.TString) {
                    "MUL called on illegal type: $poppedB!"
                }
                val poppedA = safePop(stack, "MUL2")
                check(poppedA is Type.Constant && poppedA !is Type.Constant.TString) {
                    "MUL called on illegal type: $poppedA!"
                }
                val prevB = instructions[index - 1]
                val prevA = instructions[index - 2]
                stack.push(
                    if (poppedB.constant && poppedA.constant) {
                        prevA.extra = mulNumbers(prevA.extra as Number, prevB.extra as Number)
                        deleteInstruction(oIndex, index, labels, instructions)
                        deleteInstruction(oIndex, index - 1, labels, instructions)
                        index -= 2
                        val tmp = toLarger(poppedA, poppedB)
                        tmp.constant = true
                        tmp
                    } else {
                        val tmp = toLarger(poppedA, poppedB)
                        if (poppedB.constant && (prevB.extra as Number).toDouble() == 1.0) {
                            deleteInstruction(oIndex, index - 1, labels, instructions)
                            deleteInstruction(oIndex, index, labels, instructions)
                        } else if (poppedA.constant && (prevA.extra as Number).toDouble() == 1.0) {
                            deleteInstruction(oIndex, index - 2, labels, instructions)
                            deleteInstruction(oIndex, index, labels, instructions)
                        } else if (poppedB.constant && (prevB.extra as Number).toDouble() == -1.0) {
                            deleteInstruction(oIndex, index - 1, labels, instructions)
                            instruction.opCode = Simple.NEG
                        } else if (poppedA.constant && (prevA.extra as Number).toDouble() == -1.0) {
                            deleteInstruction(oIndex, index - 2, labels, instructions)
                            instruction.opCode = Simple.NEG
                        }
                        tmp
                    }
                )
            }
            Simple.DIV -> {
                val poppedB = safePop(stack, "DIV1")
                check(poppedB is Type.Constant && poppedB !is Type.Constant.TString) {
                    "DIV called on illegal type: $poppedB!"
                }
                val poppedA = safePop(stack, "DIV2")
                check(poppedA is Type.Constant && poppedA !is Type.Constant.TString) {
                    "DIV called on illegal type: $poppedA!"
                }
                val prevB = instructions[index - 1]
                val prevA = instructions[index - 2]
                stack.push(
                    if (poppedB.constant && poppedA.constant) {
                        prevA.extra = divNumbers(prevA.extra as Number, prevB.extra as Number)
                        deleteInstruction(oIndex, index, labels, instructions)
                        deleteInstruction(oIndex, index - 1, labels, instructions)
                        index -= 2
                        val tmp = toLarger(poppedA, poppedB)
                        tmp.constant = true
                        tmp
                    } else {
                        val tmp = toLarger(poppedA, poppedB)
                        if (poppedB.constant && (prevB.extra as Number).toDouble() == 1.0) {
                            deleteInstruction(oIndex, index - 1, labels, instructions)
                            deleteInstruction(oIndex, index, labels, instructions)
                        }
                        tmp
                    }
                )
            }
            Simple.ADD -> {
                val poppedB = safePop(stack, "ADD1")
                check(poppedB is Type.Constant) {
                    "ADD called on illegal type: $poppedB!"
                }
                val poppedA = safePop(stack, "ADD2")
                check(poppedA is Type.Constant) {
                    "ADD called on illegal type: $poppedA!"
                }
                val prevB = instructions[index - 1]
                val prevA = instructions[index - 2]
                stack.push(
                    if (poppedB.constant && poppedA.constant) {
                        if (poppedB is Type.Constant.TString || poppedA is Type.Constant.TString) {
                            prevA.extra = "${prevA.extra}${prevB.extra}"
                        } else {
                            prevA.extra = addNumbers(prevA.extra as Number, prevB.extra as Number)
                        }
                        deleteInstruction(oIndex, index, labels, instructions)
                        deleteInstruction(oIndex, index - 1, labels, instructions)
                        index -= 2
                        val tmp = toLarger(poppedA, poppedB)
                        tmp.constant = true
                        tmp
                    } else {
                        if (poppedB.constant && (prevB.extra as Number).toDouble() == 1.0) {
                            deleteInstruction(oIndex, index - 1, labels, instructions)
                            instruction.opCode = Simple.INC
                        }
                        if (poppedA.constant && (prevA.extra as Number).toDouble() == 1.0) {
                            deleteInstruction(oIndex, index - 2, labels, instructions)
                            instruction.opCode = Simple.INC
                        }
                        toLarger(poppedA, poppedB)
                    }
                )
            }
            Simple.OR -> {
                val poppedB = safePop(stack, "OR1")
                check(poppedB is Type.Constant) {
                    "OR called on illegal type: $poppedB!"
                }
                val poppedA = safePop(stack, "OR2")
                check(poppedA is Type.Constant) {
                    "OR called on illegal type: $poppedA!"
                }
                stack.push(
                    if (poppedB.constant && poppedA.constant) {
                        val prevB = instructions[index - 1]
                        val prevA = instructions[index - 2]
                        prevA.extra = orNumbers(prevA.extra as Number, prevB.extra as Number)
                        deleteInstruction(oIndex, index, labels, instructions)
                        deleteInstruction(oIndex, index - 1, labels, instructions)
                        index -= 2
                        val tmp = toLarger(poppedA, poppedB)
                        tmp.constant = true
                        tmp
                    } else {
                        toLarger(poppedA, poppedB)
                    }
                )
            }
            Simple.XOR -> {
                val poppedB = safePop(stack, "XOR1")
                check(poppedB is Type.Constant) {
                    "XOR called on illegal type: $poppedB!"
                }
                val poppedA = safePop(stack, "XOR2")
                check(poppedA is Type.Constant) {
                    "XOR called on illegal type: $poppedA!"
                }
                stack.push(
                    if (poppedB.constant && poppedA.constant) {
                        val prevB = instructions[index - 1]
                        val prevA = instructions[index - 2]
                        prevA.extra = xorNumbers(prevA.extra as Number, prevB.extra as Number)
                        deleteInstruction(oIndex, index, labels, instructions)
                        deleteInstruction(oIndex, index - 1, labels, instructions)
                        index -= 2
                        val tmp = toLarger(poppedA, poppedB)
                        tmp.constant = true
                        tmp
                    } else {
                        toLarger(poppedA, poppedB)
                    }
                )
            }
            Simple.AND -> {
                val poppedB = safePop(stack, "AND1")
                check(poppedB is Type.Constant) {
                    "AND called on illegal type: $poppedB!"
                }
                val poppedA = safePop(stack, "AND2")
                check(poppedA is Type.Constant) {
                    "AND called on illegal type: $poppedA!"
                }
                stack.push(
                    if (poppedB.constant && poppedA.constant) {
                        val prevB = instructions[index - 1]
                        val prevA = instructions[index - 2]
                        prevA.extra = andNumbers(prevA.extra as Number, prevB.extra as Number)
                        deleteInstruction(oIndex, index, labels, instructions)
                        deleteInstruction(oIndex, index - 1, labels, instructions)
                        index -= 2
                        val tmp = toLarger(poppedA, poppedB)
                        tmp.constant = true
                        tmp
                    } else {
                        toLarger(poppedA, poppedB)
                    }
                )
            }
            Simple.SAR -> {
                val poppedB = safePop(stack, "SHR1")
                check(poppedB is Type.Constant.TInt) {
                    "SHR called on illegal type: $poppedB!"
                }
                val poppedA = safePop(stack, "SHR2")
                check(poppedA is Type.Constant) {
                    "SHR called on illegal type: $poppedA!"
                }
                stack.push(
                    if (poppedB.constant && poppedA.constant) {
                        val prevB = instructions[index - 1]
                        val prevA = instructions[index - 2]
                        prevA.extra = shrNumbers(prevA.extra as Number, prevB.extra as Number)
                        deleteInstruction(oIndex, index, labels, instructions)
                        deleteInstruction(oIndex, index - 1, labels, instructions)
                        index -= 2
                        val tmp = toLarger(poppedA, poppedB)
                        tmp.constant = true
                        tmp
                    } else {
                        toLarger(poppedA, poppedB)
                    }
                )
            }
            Simple.SAL -> {
                val poppedB = safePop(stack, "SHL1")
                check(poppedB is Type.Constant.TInt) {
                    "SHL called on illegal type: $poppedB!"
                }
                val poppedA = safePop(stack, "SHL2")
                check(poppedA is Type.Constant) {
                    "SHL called on illegal type: $poppedA!"
                }
                stack.push(
                    if (poppedB.constant && poppedA.constant) {
                        val prevB = instructions[index - 1]
                        val prevA = instructions[index - 2]
                        prevA.extra = shlNumbers(prevA.extra as Number, prevB.extra as Number)
                        deleteInstruction(oIndex, index, labels, instructions)
                        deleteInstruction(oIndex, index - 1, labels, instructions)
                        index -= 2
                        val tmp = toLarger(poppedA, poppedB)
                        tmp.constant = true
                        tmp
                    } else {
                        toLarger(poppedA, poppedB)
                    }
                )
            }
            Simple.SHR -> {
                val poppedB = safePop(stack, "USHR1")
                check(poppedB is Type.Constant.TInt) {
                    "USHR called on illegal type: $poppedB!"
                }
                val poppedA = safePop(stack, "SHL2")
                check(poppedA is Type.Constant) {
                    "USHR called on illegal type: $poppedA!"
                }
                stack.push(
                    if (poppedB.constant && poppedA.constant) {
                        val prevB = instructions[index - 1]
                        val prevA = instructions[index - 2]
                        prevA.extra = ushrNumbers(prevA.extra as Number, prevB.extra as Number)
                        deleteInstruction(oIndex, index, labels, instructions)
                        deleteInstruction(oIndex, index - 1, labels, instructions)
                        index -= 2
                        val tmp = toLarger(poppedA, poppedB)
                        tmp.constant = true
                        tmp
                    } else {
                        toLarger(poppedA, poppedB)
                    }
                )
            }
            Simple.ROR -> {
                val poppedB = safePop(stack, "ROR1")
                check(poppedB is Type.Constant.TInt) {
                    "ROR called on illegal type: $poppedB!"
                }
                val poppedA = safePop(stack, "ROR2")
                check(poppedA is Type.Constant) {
                    "ROR called on illegal type: $poppedA!"
                }
                stack.push(
                    if (poppedB.constant && poppedA.constant) {
                        val prevB = instructions[index - 1]
                        val prevA = instructions[index - 2]
                        prevA.extra = rorNumbers(prevA.extra as Number, prevB.extra as Number)
                        deleteInstruction(oIndex, index, labels, instructions)
                        deleteInstruction(oIndex, index - 1, labels, instructions)
                        index -= 2
                        val tmp = toLarger(poppedA, poppedB)
                        tmp.constant = true
                        tmp
                    } else {
                        toLarger(poppedA, poppedB)
                    }
                )
            }
            Simple.ROL -> {
                val poppedB = safePop(stack, "ROL1")
                check(poppedB is Type.Constant.TInt) {
                    "ROL called on illegal type: $poppedB!"
                }
                val poppedA = safePop(stack, "ROL2")
                check(poppedA is Type.Constant) {
                    "ROL called on illegal type: $poppedA!"
                }
                stack.push(
                    if (poppedB.constant && poppedA.constant) {
                        val prevB = instructions[index - 1]
                        val prevA = instructions[index - 2]
                        prevA.extra = rolNumbers(prevA.extra as Number, prevB.extra as Number)
                        deleteInstruction(oIndex, index, labels, instructions)
                        deleteInstruction(oIndex, index - 1, labels, instructions)
                        index -= 2
                        val tmp = toLarger(poppedA, poppedB)
                        tmp.constant = true
                        tmp
                    } else {
                        toLarger(poppedA, poppedB)
                    }
                )
            }
            else -> error("Unknown number command: ${instruction.opCode}")
        }
        return index
    }

    internal fun toLarger(a: Number, b: Number): Number {
        return when {
            a is Double || b is Double -> a.toDouble()
            a is Float || b is Float -> a.toFloat()
            a is Long || b is Long -> a.toLong()
            a is Int || b is Int -> a.toInt()
            a is Short || b is Short -> a.toShort()
            else -> a.toByte()
        }
    }

    internal fun toLarger(a: Type.Constant, b: Type.Constant): Type.Constant {
        return when {
            a is Type.Constant.TString || b is Type.Constant.TString -> Type.Constant.TString()
            a is Type.Constant.TDouble || b is Type.Constant.TDouble -> Type.Constant.TDouble()
            a is Type.Constant.TFloat || b is Type.Constant.TFloat -> Type.Constant.TFloat()
            a is Type.Constant.TLong || b is Type.Constant.TLong -> Type.Constant.TLong()
            a is Type.Constant.TInt || b is Type.Constant.TInt -> Type.Constant.TInt()
            a is Type.Constant.TShort || b is Type.Constant.TShort -> Type.Constant.TShort()
            else -> Type.Constant.TByte()
        }
    }

    internal fun toSameNumbers(from: Number, to: Number): Number {
        return when (to) {
            is Double -> from.toDouble()
            is Float -> from.toFloat()
            is Long -> from.toLong()
            is Int -> from.toInt()
            is Short -> from.toShort()
            else -> from.toByte()
        }
    }

    internal fun addNumbers(a: Number, b: Number): Number {
        return when (toLarger(a, b)) {
            is Double -> a.toDouble() + b.toDouble()
            is Float -> a.toFloat() + b.toFloat()
            is Long -> a.toLong() + b.toLong()
            is Int -> a.toInt() + b.toInt()
            is Short -> a.toShort() + b.toShort()
            else -> a.toByte() + b.toByte()
        }
    }

    internal fun divNumbers(a: Number, b: Number): Number {
        return when (toLarger(a, b)) {
            is Double -> a.toDouble() / b.toDouble()
            is Float -> a.toFloat() / b.toFloat()
            is Long -> a.toLong() / b.toLong()
            is Int -> a.toInt() / b.toInt()
            is Short -> (a.toShort() / b.toShort()).toShort()
            else -> (a.toByte() / b.toByte()).toByte()
        }
    }

    internal fun mulNumbers(a: Number, b: Number): Number {
        return when (toLarger(a, b)) {
            is Double -> a.toDouble() * b.toDouble()
            is Float -> a.toFloat() * b.toFloat()
            is Long -> a.toLong() * b.toLong()
            is Int -> a.toInt() * b.toInt()
            is Short -> (a.toShort() * b.toShort()).toShort()
            else -> (a.toByte() * b.toByte()).toByte()
        }
    }

    internal fun orNumbers(a: Number, b: Number): Number {
        return when (toLarger(a, b)) {
            is Long -> a.toLong() or b.toLong()
            is Int -> a.toInt() or b.toInt()
            is Short -> a.toShort() or b.toShort()
            else -> a.toByte() or b.toByte()
        }
    }

    internal fun xorNumbers(a: Number, b: Number): Number {
        return when (toLarger(a, b)) {
            is Long -> a.toLong() xor b.toLong()
            is Int -> a.toInt() xor b.toInt()
            is Short -> a.toShort() xor b.toShort()
            else -> a.toByte() xor b.toByte()
        }
    }

    internal fun andNumbers(a: Number, b: Number): Number {
        return when (toLarger(a, b)) {
            is Long -> a.toLong() and b.toLong()
            is Int -> a.toInt() and b.toInt()
            is Short -> a.toShort() and b.toShort()
            else -> a.toByte() and b.toByte()
        }
    }

    internal fun shlNumbers(a: Number, b: Number): Number {
        return when (a) {
            is Long -> a.toLong() shl b.toInt()
            is Int -> a.toInt() shl b.toInt()
            is Short -> (a.toInt() shl b.toInt()).toShort()
            else -> (a.toInt() shl b.toInt()).toByte()
        }
    }

    internal fun shrNumbers(a: Number, b: Number): Number {
        return when (a) {
            is Long -> a.toLong() shr b.toInt()
            is Int -> a.toInt() shr b.toInt()
            is Short -> (a.toInt() shr b.toInt()).toShort()
            else -> (a.toInt() shr b.toInt()).toByte()
        }
    }

    internal fun ushrNumbers(a: Number, b: Number): Number {
        return when (a) {
            is Long -> a.toLong() ushr b.toInt()
            is Int -> a.toInt() ushr b.toInt()
            is Short -> (a.toInt() ushr b.toInt()).toShort()
            else -> (a.toInt() ushr b.toInt()).toByte()
        }
    }

    internal fun subNumbers(a: Number, b: Number): Number {
        return when (toLarger(a, b)) {
            is Double -> a.toDouble() - b.toDouble()
            is Float -> a.toFloat() - b.toFloat()
            is Long -> a.toLong() - b.toLong()
            is Int -> a.toInt() - b.toInt()
            is Short -> a.toShort() - b.toShort()
            else -> a.toByte() - b.toByte()
        }
    }

    internal fun negNumber(a: Number): Number {
        return when (a) {
            is Double -> -a.toDouble()
            is Float -> -a.toFloat()
            is Long -> -a.toLong()
            is Int -> -a.toInt()
            is Short -> -a.toShort()
            else -> -a.toByte()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    internal fun rorNumbers(a: Number, b: Number): Number {
        return when (a) {
            is Long -> a.toLong().rotateRight(b.toInt())
            is Int -> a.toInt().rotateRight(b.toInt())
            is Short -> a.toShort().rotateRight(b.toInt())
            else -> a.toByte().rotateRight(b.toInt())
        }
    }


    @OptIn(ExperimentalStdlibApi::class)
    internal fun rolNumbers(a: Number, b: Number): Number {
        return when (a) {
            is Long -> a.toLong().rotateLeft(b.toInt())
            is Int -> a.toInt().rotateLeft(b.toInt())
            is Short -> a.toShort().rotateLeft(b.toInt())
            else -> a.toByte().rotateLeft(b.toInt())
        }
    }

    fun gtEq(a: Number, b: Number): Boolean {
        return when (toLarger(a, b)) {
            is Long -> a.toLong() >= b.toLong()
            is Int -> a.toInt() >= b.toInt()
            is Short -> a.toShort() >= b.toShort()
            else -> a.toByte() >= b.toByte()
        }
    }

    fun ltEq(a: Number, b: Number): Boolean {
        return when (toLarger(a, b)) {
            is Long -> a.toLong() <= b.toLong()
            is Int -> a.toInt() <= b.toInt()
            is Short -> a.toShort() <= b.toShort()
            else -> a.toByte() <= b.toByte()
        }
    }

    fun gt(a: Number, b: Number): Boolean {
        return when (toLarger(a, b)) {
            is Long -> a.toLong() > b.toLong()
            is Int -> a.toInt() > b.toInt()
            is Short -> a.toShort() > b.toShort()
            else -> a.toByte() > b.toByte()
        }
    }

    fun lt(a: Number, b: Number): Boolean {
        return when (toLarger(a, b)) {
            is Long -> a.toLong() < b.toLong()
            is Int -> a.toInt() < b.toInt()
            is Short -> a.toShort() < b.toShort()
            else -> a.toByte() < b.toByte()
        }
    }
}