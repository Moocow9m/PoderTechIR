package tech.poder.ir

import tech.poder.ir.commands.Simple
import tech.poder.ir.data.storage.Instruction
import tech.poder.ir.data.storage.Label
import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.data.ugly.StackNumberParse
import tech.poder.ir.metadata.MethodHolder
import java.util.*

class Runner {

    private fun execute(instructions: List<Instruction>, vararg args: Any): Any? {

        var index = 0
        val stack = Stack<Any>()
        val vars = args.toCollection(mutableListOf<Any?>())

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
                    /*when (inst.extra as SysCommand) {
                        SysCommand.SLEEP -> TODO()
                        SysCommand.SUSPEND -> TODO()
                        SysCommand.LOAD_LIB -> TODO()
                        SysCommand.YIELD -> TODO()
                        SysCommand.PRINT -> print(stack.pop())
                    }*/
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
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    stack.push(StackNumberParse.orNumbers(pop2, pop1))
                }
                Simple.AND -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    stack.push(StackNumberParse.andNumbers(pop2, pop1))
                }
                Simple.XOR -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    stack.push(StackNumberParse.xorNumbers(pop2, pop1))
                }
                Simple.SHL -> TODO("Kotlin VM issues")
                Simple.SHR -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    stack.push(StackNumberParse.ushrNumbers(pop2, pop1))
                }
                Simple.SAL -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    stack.push(StackNumberParse.shlNumbers(pop2, pop1))
                }
                Simple.SAR -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    stack.push(StackNumberParse.shrNumbers(pop2, pop1))
                }
                Simple.ROL -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    stack.push(StackNumberParse.rolNumbers(pop2, pop1))
                }
                Simple.ROR -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    stack.push(StackNumberParse.rorNumbers(pop2, pop1))
                }
                Simple.INC -> stack.push(StackNumberParse.addNumbers(stack.pop() as Number, 1))
                Simple.DEC -> stack.push(StackNumberParse.subNumbers(stack.pop() as Number, 1))
                Simple.ADD -> {

                    val pop1 = stack.pop()
                    val pop2 = stack.pop()

                    stack.push(
                        if (pop2 is String || pop1 is String) {
                            "$pop2$pop1"
                        } else {
                            StackNumberParse.addNumbers(pop2 as Number, pop1 as Number)
                        }
                    )

                }
                Simple.SUB -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    stack.push(StackNumberParse.subNumbers(pop2, pop1))
                }
                Simple.MUL -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    stack.push(StackNumberParse.mulNumbers(pop2, pop1))
                }
                Simple.DIV -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    stack.push(StackNumberParse.divNumbers(pop2, pop1))
                }
                Simple.NEG -> stack.push(StackNumberParse.negNumber(stack.pop() as Number))
                Simple.SWITCH -> TODO("Not Understood yet")
                Simple.IF_EQ -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    if (pop2 != pop1) {
                        index = (inst.extra as Label).offset
                    }
                }
                Simple.IF_GT -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    if (StackNumberParse.ltEq(pop2, pop1)) {
                        index = (inst.extra as Label).offset
                    }
                }
                Simple.IF_LT -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    if (StackNumberParse.gtEq(pop2, pop1)) {
                        index = (inst.extra as Label).offset
                    }
                }
                Simple.IF_GT_EQ -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    if (StackNumberParse.lt(pop2, pop1)) {
                        index = (inst.extra as Label).offset
                    }
                }
                Simple.IF_LT_EQ -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    if (StackNumberParse.gt(pop2, pop1)) {
                        index = (inst.extra as Label).offset
                    }
                }
                Simple.IF_NOT_EQ -> {
                    val pop1 = stack.pop() as Number
                    val pop2 = stack.pop() as Number
                    if (pop2 == pop1) {
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
                    /*if (holder.returnType == null) {
                        execute(holder.fullName, *newArgs)
                    } else {
                        stack.push(execute(holder.fullName, *newArgs))
                    }*/
                }
                Simple.UNSIGNED_UPSCALE -> {
                    when (val x = stack.pop() as Number) {
                        is Byte -> {
                            stack.push(x.toUByte())
                        }
                        is Short -> {
                            stack.push(x.toUShort())
                        }
                        is Int -> {
                            stack.push(x.toUInt())
                        }
                        is Long -> {
                            stack.push(x.toULong())
                        }
                    }
                }
                Simple.LAUNCH -> TODO()
                Simple.NEW_OBJECT -> {
                    /*val objDef = stack.pop() as ObjectHolder//todo add HiddenObjectHolder support
                    val fields = mutableMapOf<String, Any>()
                    repeat(objDef.fields.size) {
                        val f = objDef.fields[it]
                        fields[f.name] = f.type.defaultValue()
                    }
                    stack.push(fields)*/
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