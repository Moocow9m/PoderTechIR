package tech.poder.ir.data.storage.segment

import tech.poder.ir.commands.Simple
import tech.poder.ir.commands.SysCommand
import tech.poder.ir.data.base.Method
import tech.poder.ir.data.storage.Instruction
import tech.poder.ir.data.storage.Label
import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.data.storage.Type
import tech.poder.ir.data.ugly.StackNumberParse
import tech.poder.ir.metadata.MethodHolder
import tech.poder.ir.metadata.ObjectHolder
import java.util.*

data class SegmentPart(
    val instructions: ArrayList<Instruction> = arrayListOf()
) : Segment {
    override fun eval(
        method: Method,
        stack: Stack<Type>,
        currentVars: Array<Type?>,
        currentIndex: Int,
        labels: MutableMap<Int, Label>
    ): Int {
        var index = 0
        while (index < instructions.size) {
            val instruction = instructions[index]
            when (instruction.opCode) {
                Simple.DUP -> stack.push(stack.peek())
                Simple.POP -> safePop(stack, "POP")
                Simple.PUSH -> stack.push(toType(instruction.extra!!))
                Simple.SYS_CALL -> {
                    val call = instruction.extra as SysCommand
                    call.args.forEach {
                        val popped = safePop(stack, "SYS_CALL")
                        if (popped is Type.Constant) {
                            popped.constant = false //Consistency for compares
                        }
                        check(popped == it) {
                            "$popped does not match expected $it"
                        }
                    }
                    if (call.return_ != null) {
                        stack.push(call.return_)
                    }
                }
                Simple.SET_VAR -> {
                    val popped = safePop(stack, "SET_VAR")
                    val varId = instruction.extra as Int
                    if (popped is Type.Constant) {
                        popped.constant =
                            false //todo this should be done to a copy so other optimizers can see it is constant
                    }
                    val compare = currentVars[varId]
                    if (compare != null) {
                        check(compare == popped) {
                            "$popped does not match expected $compare"
                        }
                    }
                    currentVars[varId] = popped
                }
                Simple.GET_VAR -> {
                    val varId = instruction.extra as Int
                    stack.push(currentVars[varId])
                }
                Simple.RETURN -> {
                    if (method.returnType == null) {
                        check(stack.isEmpty()) {
                            "Stack not empty on return!\n" +
                                    "\tStack:\n" +
                                    "\t\t${stack.joinToString("\n\t\t")}"
                        }
                    } else {
                        check(stack.isNotEmpty()) {
                            "Stack empty on return when should be ${method.returnType}"
                        }
                        check(stack.size == 1) {
                            "Stack has more than 1 item on return!\n" +
                                    "\tStack:\n" +
                                    "\t\t${stack.joinToString("\n\t\t")}"
                        }
                        check(stack.peek() == method.returnType) {
                            "Stack had ${stack.pop()} instead of ${method.returnType}!"
                        }
                    }
                }
                Simple.ARRAY_CREATE -> {
                    val size = safePop(stack, "ARRAY_CREATE")
                    check(size is Type.Constant.TInt) {
                        "Array creation without Int type! Got: $size"
                    }
                    val arrayType = instruction.extra!! as Type
                    if (arrayType is Type.Constant) {
                        arrayType.constant = false
                    }
                    stack.push(
                        Type.TArray(
                            arrayType,
                            0
                        )
                    ) //todo size is unknown at this time... will be a runtime check to prevent illegal access to memory
                }
                Simple.IF_EQ, Simple.IF_GT, Simple.IF_LT,
                Simple.IF_GT_EQ, Simple.IF_LT_EQ,
                Simple.IF_NOT_EQ -> {
                    val poppedB = safePop(stack, "IF1")
                    check(poppedB is Type.Constant) {
                        "IF called on illegal type: $poppedB!"
                    }
                    val poppedA = safePop(stack, "IF2")
                    check(poppedA is Type.Constant) {
                        "IF called on illegal type: $poppedA!"
                    }
                }
                Simple.ARRAY_GET -> {
                    val array = safePop(stack, "ARRAY_GET1") as Type.TArray
                    val arrayIndex = safePop(stack, "ARRAY_GET2")
                    check(arrayIndex is Type.Constant && arrayIndex !is Type.Constant.TString) {
                        "Array get without Number type! Got: $arrayIndex"
                    }
                    stack.push(array.type)
                }
                Simple.ARRAY_SET -> {
                    val array = safePop(stack, "ARRAY_SET1") as Type.TArray
                    val arrayIndex = safePop(stack, "ARRAY_SET2")
                    val arrayItem = safePop(stack, "ARRAY_SET3")
                    check(arrayIndex is Type.Constant && arrayIndex !is Type.Constant.TString) {
                        "Array set without Number type! Got: $arrayIndex"
                    }
                    if (arrayItem is Type.Constant) {
                        arrayItem.constant = false
                    }
                    check(arrayItem == array.type) {
                        "Array set with incorrect type: $arrayItem! Wanted: ${array.type}"
                    }
                    stack.push(array.type)
                }
                Simple.LAUNCH, Simple.INVOKE_METHOD -> {
                    val holder = instruction.extra as MethodHolder
                    holder.args.forEach {
                        val popped = safePop(stack, "${instruction.opCode}_ARG_${it.name}")
                        if (popped is Type.Constant) {
                            popped.constant = false
                        }
                        check(popped == it.type) {
                            "Invalid type supplied to method! Wanted: ${it.type}, Got: $popped"
                        }
                    }

                    if (holder.returnType != null) {
                        stack.push(holder.returnType)
                    }
                }
                Simple.NEW_OBJECT -> {
                    val objType = instruction.extra as ObjectHolder
                    stack.push(Type.TStruct(objType.fullName, objType.fields))
                }
                Simple.JMP -> {
                }
                Simple.INC, Simple.DEC, Simple.SUB, Simple.MUL, Simple.DIV,
                Simple.ADD, Simple.OR, Simple.XOR, Simple.AND, Simple.SAR,
                Simple.SAL, Simple.SHR, Simple.ROR, Simple.ROL, Simple.NEG,
                Simple.SHL ->
                    index = StackNumberParse.parse(
                        index,
                        currentIndex,
                        instruction,
                        stack,
                        instructions,
                        labels
                    )
                Simple.SET_FIELD -> {
                    val object_ = safePop(stack, "SET_FIELD")
                    check(object_ is Type.TStruct) {
                        "Expected Object ref, but got: $object_"
                    }
                    val wanted = instruction.extra as NamedType
                    val result = object_.types.firstOrNull { it == wanted }
                    check(result != null) {
                        "${instruction.extra} does not exist in $object_"
                    }
                    val got = stack.pop()
                    check(wanted.type == got) {
                        "Type mismatch! Wanted: ${wanted.type}, Got: $got"
                    }
                }
                Simple.GET_FIELD -> {
                    val object_ = safePop(stack, "SET_FIELD")
                    check(object_ is Type.TStruct) {
                        "Expected Object ref, but got: $object_"
                    }
                    val result = object_.types.firstOrNull { it == (instruction.extra as NamedType) }
                    check(result != null) {
                        "${instruction.extra} does not exist in $object_"
                    }
                    stack.push(result.type)
                }
                Simple.BREAKPOINT -> {
                }
                else -> error("Unknown command: ${instruction.opCode}")
            }
            index++
        }
        return index + currentIndex
    }

    override fun size(): Int {
        return instructions.size
    }

    override fun toBulk(storage: ArrayList<Instruction>) {
        storage.addAll(instructions)
    }

    private fun toType(any: Any): Type {
        return when (any) {
            is Byte -> Type.Constant.TByte(true)
            is Short -> Type.Constant.TShort(true)
            is Int -> Type.Constant.TInt(true)
            is Long -> Type.Constant.TLong(true)
            is Float -> Type.Constant.TFloat(true)
            is Double -> Type.Constant.TDouble(true)
            is String -> Type.Constant.TString(true)
            else -> error("Unknown push: ${any::class.java}")
        }
    }

    companion object {
        internal fun safePop(stack: Stack<Type>, message: String): Type {
            check(stack.isNotEmpty()) {
                "$message could not be executed because stack was empty!"
            }
            return stack.pop()
        }

        internal fun deleteInstruction(
            offsetIndex: Int,
            currentIndex: Int,
            labels: MutableMap<Int, Label>,
            instructions: ArrayList<Instruction>
        ) {
            instructions.removeAt(currentIndex)

            val realIndex = offsetIndex + currentIndex
            labels.forEach { (index, label) ->
                val check = index..label.offset
                if (check.contains(realIndex)) {
                    label.offset = label.offset - 1
                }
            }

            labels.keys.toTypedArray().forEach {
                if (it > realIndex) {
                    val tmp = labels.remove(it)!!
                    tmp.offset = tmp.offset - 1
                    labels[it - 1] = tmp
                }
            }
        }
    }
}
