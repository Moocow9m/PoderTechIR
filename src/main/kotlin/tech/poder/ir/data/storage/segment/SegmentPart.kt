package tech.poder.ir.data.storage.segment

import tech.poder.ir.commands.Command
import tech.poder.ir.data.Label
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.unlinked.UnlinkedMethod
import java.util.*

@JvmInline
value class SegmentPart(
    val instructions: MutableList<Command> = mutableListOf()
) : Segment {

    override fun eval(
        method: UnlinkedMethod,
        stack: Stack<Type>,
        currentVars: MutableList<Type>,
        currentIndex: Int,
        labels: MutableMap<Int, Label>
    ): Int {

        var index = 0
        /*while (index < instructions.size) {

            val instruction = instructions[index]

            when (instruction.opCode) {
                Simple.DUP -> stack.push(stack.peek())
                Simple.POP -> safePop(stack, "POP")
                Simple.PUSH -> stack.push(toType(instruction.extra!!))
                Simple.SYS_CALL -> {

                    val call = instruction.extra as SysCommand

                    call.args.forEach {

                        val popped = safePop(stack, "SYS_CALL")

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
                            """
                            Stack not empty on return!
                            Stack:
                            ${stack.joinToString("\n\t\t")}
                            """.trimIndent()
                        }
                    }
                    else {
                        check(stack.isNotEmpty()) {
                            "Stack empty on return when should be ${method.returnType}"
                        }
                        check(stack.size == 1) {
                            """
                            Stack has more than 1 item on return!
                            Stack: 
                            ${stack.joinToString("\n\t\t")}    
                            """.trimIndent()
                        }
                        check(stack.peek() == method.returnType) {
                            "Stack had ${stack.pop()} instead of ${method.returnType}!"
                        }
                    }
                }

                Simple.ARRAY_CREATE -> {

                    val size = safePop(stack, "ARRAY_CREATE")
                    val arrayType = instruction.extra!! as Type

                    check(size is Type.Primitive.Int) {
                        "Array creation without Int type! Got: $size"
                    }

                    // TODO: size is unknown at this time... will be a runtime check to prevent illegal access to memory
                    stack.push(Type.Array(arrayType, 0))
                }

                Simple.IF_EQ, Simple.IF_GT, Simple.IF_LT,
                Simple.IF_GT_EQ, Simple.IF_LT_EQ,
                Simple.IF_NOT_EQ,
                -> {

                    val poppedB = safePop(stack, "IF1")
                    val poppedA = safePop(stack, "IF2")

                    check(poppedB is Type.Primitive) {
                        "IF called on illegal type: $poppedB!"
                    }
                    check(poppedA is Type.Primitive) {
                        "IF called on illegal type: $poppedA!"
                    }
                }

                Simple.ARRAY_GET -> {

                    val array = safePop(stack, "ARRAY_GET1") as Type.Array
                    val arrayIndex = safePop(stack, "ARRAY_GET2")

                    check(arrayIndex is Type.Primitive && arrayIndex !is Type.Primitive.String) {
                        "Array get without Number type! Got: $arrayIndex"
                    }

                    stack.push(array.type)
                }
                Simple.ARRAY_SET -> {

                    val array = safePop(stack, "ARRAY_SET1") as Type.Array
                    val arrayIndex = safePop(stack, "ARRAY_SET2")
                    val arrayItem = safePop(stack, "ARRAY_SET3")

                    check(arrayIndex is Type.Primitive && arrayIndex !is Type.Primitive.String) {
                        "Array set without Number type! Got: $arrayIndex"
                    }

                    check(arrayItem == array.type) {
                        "Array set with incorrect type: $arrayItem! Wanted: ${array.type}"
                    }

                    stack.push(array.type)
                }

                Simple.UNSIGNED_UPSCALE -> {
                    val type = stack.pop()
                    check(type is Type.Primitive) {
                        "Upscale on a $type?"
                    }
                    when (type) {
                        is Type.Primitive.Double, is Type.Primitive.Float, is Type.Primitive.String -> error("Doesn't work on $type")
                        else -> stack.push(type)
                    }
                }

                Simple.LAUNCH, Simple.INVOKE_METHOD -> {

                    val holder = instruction.extra as MethodHolder

                    holder.args.forEach {
                        val popped = safePop(stack, "${instruction.opCode}_ARG_${it.name}")

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
                    stack.push(Type.Struct(objType.fullName, objType.fields))
                }

                Simple.JMP -> {
                }

                Simple.INC, Simple.DEC, Simple.SUB, Simple.MUL, Simple.DIV,
                Simple.ADD, Simple.OR, Simple.XOR, Simple.AND, Simple.SAR,
                Simple.SAL, Simple.SHR, Simple.ROR, Simple.ROL, Simple.NEG,
                Simple.SHL,
                -> {
                    index = StackNumberParse.parse(
                        index,
                        currentIndex,
                        instruction,
                        stack,
                        instructions,
                        labels
                    )
                }

                Simple.SET_FIELD -> {

                    val object_ = safePop(stack, "SET_FIELD")
                    val wanted = instruction.extra as NamedType
                    val pop = stack.pop()

                    check(object_ is Type.Struct) {
                        "Expected Object ref, but got: $object_"
                    }
                    check(wanted in object_.types) {
                        "${instruction.extra} does not exist in $object_"
                    }
                    check(wanted.type == pop) {
                        "Type mismatch! Wanted: ${wanted.type}, Got: $pop"
                    }
                }

                Simple.GET_FIELD -> {

                    val object_ = safePop(stack, "SET_FIELD")

                    check(object_ is Type.Struct) {
                        "Expected Object ref, but got: $object_"
                    }

                    val result = checkNotNull(object_.types.firstOrNull { it == instruction.extra }) {
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

    override fun toBulk(storage: MutableList<Instruction>) {
        storage.addAll(instructions)
    }

    private fun toType(any: Any): Type {
        return when (any) {
            is Byte -> Type.Primitive.Byte(true)
            is Short -> Type.Primitive.Short(true)
            is Int -> Type.Primitive.Int(true)
            is Long -> Type.Primitive.Long(true)
            is Float -> Type.Primitive.Float(true)
            is Double -> Type.Primitive.Double(true)
            is String -> Type.Primitive.String(true)
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
            instructions: MutableList<Instruction>
        ) {

            instructions.removeAt(currentIndex)

            val realIndex = offsetIndex + currentIndex

            labels.forEach { (index, label) ->
                if (realIndex in index..label.offset) {
                    label.offset = label.offset - 1
                }
            }

            labels.keys.filter { it > realIndex }.forEach {
                labels[it - 1] = labels.remove(it)!!.apply {
                    offset--
                }
            }
        }*/
        return 0
    }

    override fun size(): Int {
        TODO("Not yet implemented")
    }

    override fun toBulk(storage: MutableList<Command>) {
        TODO("Not yet implemented")
    }
}
