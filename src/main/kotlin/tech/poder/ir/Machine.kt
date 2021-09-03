package tech.poder.ir

import tech.poder.ir.commands.Simple
import tech.poder.ir.commands.SysCommand
import tech.poder.ir.data.base.Package
import tech.poder.ir.data.storage.Instruction
import tech.poder.ir.data.storage.Label
import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.data.storage.memory.MemoryAllocator
import tech.poder.ir.data.ugly.StackNumberParse
import tech.poder.ir.metadata.MethodHolder
import tech.poder.ir.metadata.ObjectHolder
import java.util.*

class Machine(maxMemory: Long = 1_073_741_824 /* 1 GB default*/, pageSize: Long = 1_024 /*1 KB default*/) {

    private val methods = mutableMapOf<String, List<Instruction>>()
    private val structs = mutableMapOf<String, List<NamedType>>()
    private val allocator = MemoryAllocator(maxMemory, pageSize)

    fun loadPackage(package_: Package) {
        package_.floating.forEach {
            val list = mutableListOf<Instruction>()
            it.toBulk(list)
            methods[it.fullName] = list
        }

        package_.objects.forEach { obj ->
            structs[obj.fullName] = obj.fields
            obj.methods.forEach {
                val list = ArrayList<Instruction>()
                it.toBulk(list)
                methods[it.fullName] = list
            }
        }
    }

    fun execute(name: String, vararg args: Any) {

        val method = checkNotNull(methods[name]) {
            "Method \"$name\" does not exist!"
        }

        execute(method, *args)
    }

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