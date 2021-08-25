package tech.poder.ir.vm

import tech.poder.ir.instructions.common.Instruction
import tech.poder.ir.instructions.common.Method
import tech.poder.ir.instructions.common.Object
import tech.poder.ir.instructions.common.Struct
import tech.poder.ir.instructions.common.special.SpecialCalls
import tech.poder.ir.instructions.complex.Complex
import tech.poder.ir.instructions.simple.Simple
import java.util.*
import java.util.concurrent.ThreadLocalRandom

object Machine {
    private val loadedMethods = mutableMapOf<String, Method>()
    private val loadedObjects = mutableMapOf<String, Object>()

    fun loadCode(vararg methods: Method) {
        methods.forEach {
            val key = it.fullName
            check(!loadedMethods.containsKey(key)) {
                "Duplicate method: $key"
            }
            loadedMethods[key] = it
        }
    }

    fun clear() {
        loadedMethods.clear()
    }

    fun loadCode(vararg objects: Object) {
        val methods = mutableSetOf<Method>()
        objects.forEach {
            methods.addAll(it.methods)
            check(!loadedObjects.containsKey(it.nameSpace)) {
                "Duplicate object: ${it.nameSpace}"
            }
            loadedObjects[it.nameSpace] = it
        }
        loadCode(*methods.toTypedArray())
    }

    fun execute(target: String, vararg args: Any): Any? {
        val method = loadedMethods[target]
        if (method == null) {
            throw IllegalStateException("Method does not exist: $method")
        }
        return execute(method, *args)
    }

    private fun execute(method: Method, vararg args: Any): Any? {
        val data = method.parent.struct
        val tmpStack = Stack<Any>()
        val variables = mutableMapOf<UByte, Any>()
        if (data != null) {
            variables[0u] = data
        }

        if (args.isNotEmpty()) {
            variables[1u] = args
        }

        var index = 0
        while (index < method.code.size) {
            index += executeInstruction(tmpStack, variables, method.code.size, method.code[index])
            index++
        }

        return if (method.returns) {
            check(tmpStack.isNotEmpty()) {
                "Method stack was empty at return of value!"
            }
            val tmp = tmpStack.pop()
            if (tmpStack.isNotEmpty()) {
                System.err.println("[WARN] Stack had leftover items!\nStack:\n\t${tmpStack.joinToString("\n\t")}")
            }
            tmp
        } else {
            if (tmpStack.isNotEmpty()) {
                System.err.println("[WARN] Stack had leftover items!\nStack:\n\t${tmpStack.joinToString("\n\t")}")
            }
            null
        }
    }

    private fun executeInstruction(
        stack: Stack<Any>,
        localVars: MutableMap<UByte, Any>,
        end: Int,
        instruction: Instruction
    ): Int {
        when (instruction.opcode) {
            Simple.ADD -> {
                val b = stack.pop()
                val a = stack.pop()
                stack.push(
                    if (a is String || b is String || a is Char || b is Char) {
                        "$a$b"
                    } else {
                        addNumbers(a as Number, b as Number)
                    }
                )
            }
            Simple.SUB -> {
                val b = stack.pop()
                val a = stack.pop()
                stack.push(subNumbers(a as Number, b as Number))
            }
            Simple.DEC -> {
                val a = stack.pop()
                stack.push(subNumbers(a as Number, 1))
            }
            Simple.INC -> {
                val a = stack.pop()
                stack.push(addNumbers(a as Number, 1))
            }
            Simple.MUL -> {
                val b = stack.pop()
                val a = stack.pop()
                stack.push(mulNumbers(a as Number, b as Number))
            }
            Simple.DIV -> {
                val b = stack.pop()
                val a = stack.pop()
                stack.push(divNumbers(a as Number, b as Number))
            }
            Simple.DUP -> {
                stack.push(stack.peek())
            }
            Simple.PUSH -> {
                stack.push(instruction.extra.first())
            }
            Simple.INVOKE_METHOD -> {
                val method = instruction.extra[0] as String
                val args = mutableListOf<Any>()
                repeat(instruction.extra[1] as Int) {
                    args.add(stack.pop())
                }
                val tmp = execute(method, *args.toTypedArray())
                if (tmp != null) {
                    stack.push(tmp)
                }
            }
            Complex.SYS_CALL -> {
                when (val call = instruction.extra.first() as SpecialCalls) {
                    SpecialCalls.PRINT -> {
                        print(stack.pop())
                    }
                    SpecialCalls.SYSTEM_IN -> {
                        if (instruction.extra.isNotEmpty()) {
                            val byteArray = ByteArray(instruction.extra.first() as Int)
                            System.`in`.read(byteArray)
                            stack.push(byteArray)
                        } else {
                            stack.push(System.`in`.read())
                        }
                    }
                    SpecialCalls.RANDOM_INT -> {
                        var max = Int.MAX_VALUE
                        var min = Int.MIN_VALUE
                        if (instruction.extra.size == 2) {
                            max = instruction.extra[1] as Int
                        } else if (instruction.extra.size == 3) {
                            min = instruction.extra[1] as Int
                            max = instruction.extra[2] as Int
                        }
                        stack.push(ThreadLocalRandom.current().nextInt(min, max))
                    }
                    else -> error("Unrecognized call: $call")
                }
            }
            Complex.COMPARE -> {
                val b = stack.pop()
                val a = stack.pop()
                val aNum = processToNumeric(a)
                val bNum = processToNumeric(b)
                stack.push(compareNumbers(aNum, bNum))
            }
            Simple.IF_GT -> {
                executeInstruction(stack, localVars, end, compare)
                val num = stack.pop()
                if (num as Int <= 0) {
                    return instruction.extra.first() as Int
                }
            }
            Simple.IF_GT_EQ -> {
                executeInstruction(stack, localVars, end, compare)
                val num = stack.pop()
                if ((num as Int) < 0) {
                    return instruction.extra.first() as Int
                }
            }
            Simple.IF_LT -> {
                executeInstruction(stack, localVars, end, compare)
                val num = stack.pop()
                if (num as Int >= 0) {
                    return instruction.extra.first() as Int
                }
            }
            Simple.IF_LT_EQ -> {
                executeInstruction(stack, localVars, end, compare)
                val num = stack.pop()
                if ((num as Int) > 0) {
                    return instruction.extra.first() as Int
                }
            }
            Simple.IF_EQ -> {
                executeInstruction(stack, localVars, end, compare)
                val num = stack.pop()
                if (num as Int != 0) {
                    return instruction.extra.first() as Int
                }
            }
            Simple.IF_NEQ -> {
                executeInstruction(stack, localVars, end, compare)
                val num = stack.pop()
                if (num as Int == 0) {
                    return instruction.extra.first() as Int
                }
            }
            Simple.JMP -> {
                return instruction.extra.first() as Int
            }
            Simple.RETURN -> {
                return end
            }
            Simple.POP -> {
                stack.pop()
            }
            Simple.STORE_VAR -> {
                localVars[instruction.extra.first() as UByte] = stack.pop()
            }
            Simple.GET_VAR -> {
                stack.push(localVars[instruction.extra.first() as UByte])
            }
            Simple.ARRAY_GET -> {
                val array = stack.pop() as Array<out Any>
                stack.push(array[stack.pop() as Int])
            }
            Simple.NEW_OBJECT -> {
                val objName = instruction.extra.first() as String
                val obj = loadedObjects[objName]
                check(obj != null) {
                    "Could not find: $objName!"
                }
                stack.push(obj.createInstance())
                executeInstruction(stack, localVars, end, dup)
                executeInstruction(stack, localVars, end, Instruction.create(Simple.INVOKE_METHOD, "init", 1))
            }
            else -> {
                TODO("Instruction: ${instruction.opcode}")
            }
        }
        return 0
    }

    private val compare = Instruction(Complex.COMPARE)
    private val dup = Instruction(Simple.DUP)

    private fun loadStruct(struct: Struct?): ByteArray? {
        return if (struct == null) {
            null
        } else {
            TODO("STUCTS NOT SUPPORTED YET")
        }
    }

    private fun processToNumeric(item: Any): Number {
        return if (item is Number) {
            item
        } else if (item is Boolean) {
            if (item) {
                1
            } else {
                0
            }
        } else {
            item.hashCode()
        }
    }

    internal fun toLarger(a: Number, b: Number): Number {
        return when {
            a is Double || b is Double -> {
                a.toDouble()
            }
            a is Float || b is Float -> {
                a.toFloat()
            }
            a is Long || b is Long -> {
                a.toLong()
            }
            a is Int || b is Int -> {
                a.toInt()
            }
            a is Short || b is Short -> {
                a.toShort()
            }
            else -> {
                a.toByte()
            }
        }
    }

    private fun compareNumbers(a: Number, b: Number): Int {
        return when (toLarger(a, b)) {
            is Double -> {
                a.toDouble().compareTo(b.toDouble())
            }
            is Float -> {
                a.toFloat().compareTo(b.toFloat())
            }
            is Long -> {
                a.toLong().compareTo(b.toLong())
            }
            else -> {
                a.toInt()
                    .compareTo(b.toInt()) //int is a default case in cpu, so this may be easier than using smaller numbers... larger memory though
            }
        }
    }

    private fun addNumbers(a: Number, b: Number): Number {
        return when (toLarger(a, b)) {
            is Double -> {
                a.toDouble() + b.toDouble()
            }
            is Float -> {
                a.toFloat() + b.toFloat()
            }
            is Long -> {
                a.toLong() + b.toLong()
            }
            is Int -> {
                a.toInt() + b.toInt()
            }
            is Short -> {
                a.toShort() + b.toShort()
            }
            else -> {
                a.toByte() + b.toByte()
            }
        }
    }

    private fun subNumbers(a: Number, b: Number): Number {
        return when (toLarger(a, b)) {
            is Double -> {
                a.toDouble() - b.toDouble()
            }
            is Float -> {
                a.toFloat() - b.toFloat()
            }
            is Long -> {
                a.toLong() - b.toLong()
            }
            is Int -> {
                a.toInt() - b.toInt()
            }
            is Short -> {
                a.toShort() - b.toShort()
            }
            else -> {
                a.toByte() - b.toByte()
            }
        }
    }

    private fun mulNumbers(a: Number, b: Number): Number {
        return when (toLarger(a, b)) {
            is Double -> {
                a.toDouble() * b.toDouble()
            }
            is Float -> {
                a.toFloat() * b.toFloat()
            }
            is Long -> {
                a.toLong() * b.toLong()
            }
            is Int -> {
                a.toInt() * b.toInt()
            }
            is Short -> {
                a.toShort() * b.toShort()
            }
            else -> {
                a.toByte() * b.toByte()
            }
        }
    }

    private fun divNumbers(a: Number, b: Number): Number {
        return when (toLarger(a, b)) {
            is Double -> {
                a.toDouble() / b.toDouble()
            }
            is Float -> {
                a.toFloat() / b.toFloat()
            }
            is Long -> {
                a.toLong() / b.toLong()
            }
            is Int -> {
                a.toInt() / b.toInt()
            }
            is Short -> {
                a.toShort() / b.toShort()
            }
            else -> {
                a.toByte() / b.toByte()
            }
        }
    }
}