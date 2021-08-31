package tech.poder.ir

import tech.poder.ir.commands.Simple
import tech.poder.ir.commands.SysCommand
import tech.poder.ir.data.base.Package
import tech.poder.ir.data.storage.Instruction
import tech.poder.ir.data.storage.Label
import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.data.ugly.StackNumberParse
import tech.poder.ir.metadata.MethodHolder
import tech.poder.ir.metadata.ObjectHolder
import java.util.*

class Machine {
    private val methods = mutableMapOf<String, Array<Instruction>>()
    private val structs = mutableMapOf<String, Array<NamedType>>()

    fun loadPackage(package_: Package) {
        package_.floating.forEach {
            val list = ArrayList<Instruction>()
            it.toBulk(list)
            methods[it.fullName] = list.toTypedArray()
        }

        package_.objects.forEach { obj ->
            structs[obj.fullName] = obj.fields
            obj.methods.forEach {
                val list = ArrayList<Instruction>()
                it.toBulk(list)
                methods[it.fullName] = list.toTypedArray()
            }
        }
    }

    fun execute(name: String, vararg args: Any) {
        val method = methods[name]
        check(method != null) {
            "Method \"$name\" does not exist!"
        }
        execute(method, *args)
    }

    private fun execute(instructions: Array<Instruction>, vararg args: Any): Any? {
        var index = 0
        val stack = Stack<Any>()
        val vars = ArrayList<Any?>()
        args.forEach {
            vars.add(it)
        }
        while (index < instructions.size) {
            val inst = instructions[index]
            when (inst.opCode) {
                Simple.JMP -> {
                    index = (inst.extra as Number).toInt()
                }
                Simple.RETURN -> {
                    return if (stack.isEmpty()) {
                        null
                    } else {
                        stack.pop()
                    }
                }
                Simple.SYS_CALL -> {
                    when (inst.extra as SysCommand) {
                        SysCommand.SLEEP -> TODO()
                        SysCommand.SUSPEND -> TODO()
                        SysCommand.LOAD_LIB -> TODO()
                        SysCommand.YIELD -> TODO()
                        SysCommand.PRINT -> print(stack.pop())
                    }
                }
                Simple.PUSH -> {
                    stack.push(inst.extra)
                }
                Simple.POP -> {
                    stack.pop()
                }
                Simple.DUP -> {
                    stack.push(stack.peek())
                }
                Simple.OR -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    stack.push(StackNumberParse.orNumbers(a, b))
                }
                Simple.AND -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    stack.push(StackNumberParse.andNumbers(a, b))
                }
                Simple.XOR -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    stack.push(StackNumberParse.xorNumbers(a, b))
                }
                Simple.SHL -> TODO("Kotlin VM issues")
                Simple.SHR -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    stack.push(StackNumberParse.ushrNumbers(a, b))
                }
                Simple.SAL -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    stack.push(StackNumberParse.shlNumbers(a, b))
                }
                Simple.SAR -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    stack.push(StackNumberParse.shrNumbers(a, b))
                }
                Simple.ROL -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    stack.push(StackNumberParse.rolNumbers(a, b))
                }
                Simple.ROR -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    stack.push(StackNumberParse.rorNumbers(a, b))
                }
                Simple.INC -> stack.push(StackNumberParse.addNumbers(stack.pop() as Number, 1))
                Simple.DEC -> stack.push(StackNumberParse.subNumbers(stack.pop() as Number, 1))
                Simple.ADD -> {
                    val b = stack.pop()
                    val a = stack.pop()
                    stack.push(
                        if (a is String || b is String) {
                            "$a$b"
                        } else {
                            StackNumberParse.addNumbers(a as Number, b as Number)
                        }
                    )

                }
                Simple.SUB -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    stack.push(StackNumberParse.subNumbers(a, b))
                }
                Simple.MUL -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    stack.push(StackNumberParse.mulNumbers(a, b))
                }
                Simple.DIV -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    stack.push(StackNumberParse.divNumbers(a, b))
                }
                Simple.NEG -> stack.push(StackNumberParse.negNumber(stack.pop() as Number))
                Simple.SWITCH -> TODO("Not Understood yet")
                Simple.IF_EQ -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    if (a != b) {
                        index = (inst.extra as Label).offset
                    }
                }
                Simple.IF_GT -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    if (StackNumberParse.ltEq(a, b)) {
                        index = (inst.extra as Label).offset
                    }
                }
                Simple.IF_LT -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    if (StackNumberParse.gtEq(a, b)) {
                        index = (inst.extra as Label).offset
                    }
                }
                Simple.IF_GT_EQ -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    if (StackNumberParse.lt(a, b)) {
                        index = (inst.extra as Label).offset
                    }
                }
                Simple.IF_LT_EQ -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    if (StackNumberParse.gt(a, b)) {
                        index = (inst.extra as Label).offset
                    }
                }
                Simple.IF_NOT_EQ -> {
                    val b = stack.pop() as Number
                    val a = stack.pop() as Number
                    if (a == b) {
                        index = (inst.extra as Label).offset
                    }
                }
                Simple.ARRAY_SET -> {
                    val array = stack.pop() as Array<Any>
                    array[(stack.pop() as Number).toInt()] = stack.pop()
                }
                Simple.ARRAY_GET -> {
                    val array = stack.pop() as Array<*>
                    stack.push(array[(stack.pop() as Number).toInt()])
                }
                Simple.ARRAY_CREATE -> {
                    stack.push(Array<Any?>((stack.pop() as Number).toInt()) {
                        null
                    })
                }
                Simple.SET_VAR -> {
                    val varIndex = inst.extra as Int
                    while (varIndex >= vars.size) {
                        vars.add(null)
                    }
                    vars[varIndex] = stack.pop()
                }
                Simple.GET_VAR -> stack.push(vars[inst.extra as Int])
                Simple.SET_FIELD -> {
                    val object_ = stack.pop() as MutableMap<String, Any>
                    object_[(inst.extra as NamedType).name] = stack.pop()
                }
                Simple.GET_FIELD -> {
                    val object_ = stack.pop() as Map<String, Any>
                    stack.push(object_[(inst.extra as NamedType).name])
                }
                Simple.INVOKE_METHOD -> {
                    val holder = inst.extra as MethodHolder //todo add HiddenMethodHolder support
                    val newArgs = Array(holder.args.size) {
                        stack.pop()
                    }
                    if (holder.returnType == null) {
                        execute(holder.fullName, *newArgs)
                    } else {
                        stack.push(execute(holder.fullName, *newArgs))
                    }
                }
                Simple.LAUNCH -> TODO()
                Simple.NEW_OBJECT -> {
                    val objDef = stack.pop() as ObjectHolder//todo add HiddenObjectHolder support
                    val fields = mutableMapOf<String, Any>()
                    repeat(objDef.fields.size) {
                        val f = objDef.fields[it]
                        fields[f.name] = f.type.defaultValue()
                    }
                    stack.push(fields)
                }
                Simple.BREAKPOINT -> {
                    System.err.println("BREAKPOINT")
                }
            }
            index++
        }

        return if (stack.isEmpty()) {
            null
        } else {
            stack.pop()
        }
    }
}