package tech.poder.ptir.data

import tech.poder.ptir.commands.Command
import tech.poder.ptir.commands.Simple
import tech.poder.ptir.commands.SysCommand
import tech.poder.ptir.data.base.Method
import tech.poder.ptir.data.base.Object
import tech.poder.ptir.data.base.Package
import tech.poder.ptir.data.math.StackNumberParse.parse
import tech.poder.ptir.data.storage.Instruction
import tech.poder.ptir.data.storage.Label
import tech.poder.ptir.data.storage.NamedType
import tech.poder.ptir.data.storage.Type
import tech.poder.ptir.data.storage.segment.MultiSegment
import tech.poder.ptir.data.storage.segment.Segment
import tech.poder.ptir.metadata.MethodHolder
import tech.poder.ptir.metadata.ObjectHolder
import tech.poder.ptir.metadata.Visibility
import java.util.*

data class CodeBuilder(
    private val storage: Method,
    val instructions: ArrayList<Instruction> = arrayListOf()
) {
    companion object {
        fun createMethod(
            package_: Package,
            name: String,
            vis: Visibility,
            returnType: Type? = null,
            args: Set<NamedType> = emptySet(),
            parent: Object? = null,
            block: (CodeBuilder) -> Unit
        ): Method {
            val method = Method(package_, parent, name, returnType, args, vis, 0, MultiSegment())

            val builder = CodeBuilder(method)
            block.invoke(builder)

            method.instructions = builder.finalize()
            return method
        }

        private val constantMethods = mutableMapOf<Command, Instruction>()
        private fun getOrPut(command: Command): Instruction {
            return constantMethods.getOrPut(command) {
                Instruction(command)
            }
        }

        private fun validateStack(
            builder: CodeBuilder,
            instructions: ArrayList<Instruction>,
            currentStack: Stack<Type>? = null,
            currentVars: Array<Type?>? = null
        ): Stack<Type> {
            val stack = currentStack ?: Stack()
            val vars: Array<Type?> = currentVars ?: Array(builder.localVars.size) {
                null
            }
            if (builder.storage.args.isNotEmpty() && vars[0] == null && builder.storage.args.isNotEmpty()) {
                builder.storage.args.forEach {
                    vars[builder.localVars.indexOf(it.name)] = it.type
                }
            }
            var index = 0
            val labels = mutableMapOf<Int, Label>()
            instructions.forEachIndexed { i, instruction ->
                if (instruction.extra is Label) {
                    labels[i] = instruction.extra as Label
                }
            }
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
                        val compare = vars[varId]
                        if (compare != null) {
                            check(compare == popped) {
                                "$popped does not match expected $compare"
                            }
                        }
                        vars[varId] = popped
                    }
                    Simple.GET_VAR -> {
                        val varId = instruction.extra as Int
                        stack.push(vars[varId])
                    }
                    Simple.RETURN -> {
                        if (builder.storage.returnType == null) {
                            check(stack.isEmpty()) {
                                "Stack not empty on return!\n" +
                                        "\tStack:\n" +
                                        "\t\t${stack.joinToString("\n\t\t")}"
                            }
                        } else {
                            check(stack.isNotEmpty()) {
                                "Stack empty on return when should be ${builder.storage.returnType}"
                            }
                            check(stack.size == 1) {
                                "Stack has more than 1 item on return!\n" +
                                        "\tStack:\n" +
                                        "\t\t${stack.joinToString("\n\t\t")}"
                            }
                            check(stack.peek() == builder.storage.returnType) {
                                "Stack had ${stack.pop()} instead of ${builder.storage.returnType}!"
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
                        ) //todo size is unknown at this time... will be a runtime check to prevent illegal access
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
                        check(arrayIndex is Type.Constant.TInt) {
                            "Array get without Int type! Got: $arrayIndex"
                        }
                        stack.push(array.type)
                    }
                    Simple.ARRAY_SET -> {
                        val array = safePop(stack, "ARRAY_SET1") as Type.TArray
                        val arrayIndex = safePop(stack, "ARRAY_SET2")
                        val arrayItem = safePop(stack, "ARRAY_SET3")
                        check(arrayIndex is Type.Constant.TInt) {
                            "Array set without Int type! Got: $arrayIndex"
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
                        stack.push(Type.TStruct(objType.fields))
                    }
                    Simple.JMP -> {
                    }
                    Simple.INC, Simple.DEC, Simple.SUB, Simple.MUL, Simple.DIV,
                    Simple.ADD, Simple.OR, Simple.XOR, Simple.AND, Simple.SAR,
                    Simple.SAL, Simple.SHR, Simple.ROR, Simple.ROL, Simple.NEG,
                    Simple.SHL ->
                        index = parse(
                            index,
                            instruction,
                            stack,
                            instructions,
                            labels
                        )
                    else -> error("Unknown command: ${instruction.opCode}")
                }
                index++
            }
            return stack
        }

        internal fun scanLabels(removedIndex: Int, labels: MutableMap<Int, Label>) {
            labels.forEach { (index, label) ->
                val check = index..label.offset
                if (check.contains(removedIndex)) {
                    label.offset = label.offset - 1//todo verify this
                }
            }

            labels.keys.toTypedArray().forEach {
                if (it > removedIndex) {
                    val tmp = labels.remove(it)!!
                    labels[it - 1] = tmp
                }
            }
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

        internal fun safePop(stack: Stack<Type>, message: String): Type {
            check(stack.isNotEmpty()) {
                "$message could not be executed because stack was empty!"
            }
            return stack.pop()
        }
    }

    private var localVars = ArrayList<String>()

    init {
        storage.args.forEach {
            localVars.add(it.name)
        }
    }

    val fullName by lazy {
        storage.fullName
    }

    //Bit Operations
    fun and() {
        instructions.add(getOrPut(Simple.AND))
    }

    fun or() {
        instructions.add(getOrPut(Simple.OR))
    }

    fun xor() {
        instructions.add(getOrPut(Simple.XOR))
    }

    fun unsignedShiftLeft() {
        instructions.add(getOrPut(Simple.SHL))
    }

    fun signedShiftLeft() {
        instructions.add(getOrPut(Simple.SAL))
    }

    fun signedShiftRight() {
        instructions.add(getOrPut(Simple.SAR))
    }

    fun unsignedShiftRight() {
        instructions.add(getOrPut(Simple.SHR))
    }

    fun rotateRight() {
        instructions.add(getOrPut(Simple.ROR))
    }

    fun rotateLeft() {
        instructions.add(getOrPut(Simple.ROL))
    }

    //Math
    fun inc() {
        instructions.add(getOrPut(Simple.INC))
    }

    fun add() {
        instructions.add(getOrPut(Simple.ADD))
    }

    fun mul() {
        instructions.add(getOrPut(Simple.MUL))
    }

    fun dec() {
        instructions.add(getOrPut(Simple.DEC))
    }

    fun sub() {
        instructions.add(getOrPut(Simple.SUB))
    }

    fun div() {
        instructions.add(getOrPut(Simple.DIV))
    }

    fun neg() {
        instructions.add(getOrPut(Simple.NEG))
    }

    //Labels
    private var lastLabel: UShort = 0u

    fun newLabel(): Label {
        return Label(lastLabel++)
    }

    fun jmp(to: Label) {
        instructions.add(Instruction(Simple.JMP, to))
    }

    fun placeLabel(label: Label) {
        label.use(instructions.size - 1)
    }

    //Local Vars
    fun setVar(name: String) {
        if (!localVars.contains(name)) {
            localVars.add(name)
        }
        instructions.add(Instruction(Simple.SET_VAR, localVars.indexOf(name)))
    }

    fun getVar(name: String) {
        check(localVars.contains(name)) {
            "Variable $name does not have a value on get!\n\tKnown Variables:\n\t\t${localVars.joinToString("\n\t\t")}"
        }
        instructions.add(Instruction(Simple.GET_VAR, localVars.indexOf(name)))
    }

    //Array
    fun getArrayItem() {
        instructions.add(getOrPut(Simple.ARRAY_GET))
    }

    fun setArrayItem() {
        instructions.add(getOrPut(Simple.ARRAY_SET))
    }

    fun createArray(type: Type) {
        instructions.add(Instruction(Simple.ARRAY_CREATE, type))
    }

    //Fields
    private fun getFieldType(name: String): NamedType {
        check(storage.parent != null) {
            "Floating method has no fields!"
        }
        val fName = "${storage.parent.fullName}.$name"
        return storage.parent.fields.first { it.name == fName }
    }

    fun getField(name: String) {
        getField(getFieldType(name))
    }

    fun setField(name: String) {
        setField(getFieldType(name))
    }

    fun getField(field: NamedType) {
        instructions.add(Instruction(Simple.GET_FIELD, field))
    }

    fun setField(field: NamedType) {
        instructions.add(Instruction(Simple.SET_FIELD, field))
    }

    //Statements
    fun ifEquals(elseJump: Label) { //todo all statements should have fragments for parsing ease
        instructions.add(Instruction(Simple.IF_EQ, elseJump))
    }

    fun ifNotEquals(elseJump: Label) {
        instructions.add(Instruction(Simple.IF_NOT_EQ, elseJump))
    }

    fun ifGreaterThan(elseJump: Label) {
        instructions.add(Instruction(Simple.IF_GT, elseJump))
    }

    fun ifLessThan(elseJump: Label) {
        instructions.add(Instruction(Simple.IF_LT, elseJump))
    }

    fun ifGreaterThanEqual(elseJump: Label) {
        instructions.add(Instruction(Simple.IF_GT_EQ, elseJump))
    }

    fun ifLessThanEqual(elseJump: Label) {
        instructions.add(Instruction(Simple.IF_LT_EQ, elseJump))
    }

    fun switch(lowCase: Int, highCase: Int, default: Label, vararg cases: Label) {
        instructions.add(Instruction(Simple.SWITCH, arrayOf(lowCase, highCase, default, *cases)))
    }

    //Methods
    fun invokeMethod(method: Method) {
        instructions.add(
            Instruction(
                Simple.INVOKE_METHOD,
                MethodHolder(method.fullName, method.returnType, method.args)
            )
        )
    }

    fun invokeMethod(fullName: String, returnType: Type? = null, vararg args: NamedType) {
        instructions.add(Instruction(Simple.INVOKE_METHOD, MethodHolder(fullName, returnType, args.toSet())))
    }

    fun launch(method: Method) {
        check(method.returnType == null) {
            "Launched methods cannot have a return type!"
        }
        instructions.add(
            Instruction(
                Simple.LAUNCH,
                MethodHolder(method.fullName, method.returnType, method.args)
            )
        )
    }

    fun launch(fullName: String, vararg args: NamedType) {
        instructions.add(Instruction(Simple.LAUNCH, MethodHolder(fullName, null, args.toSet())))
    }

    //Misc
    fun return_() {
        instructions.add(getOrPut(Simple.RETURN))
    }

    fun newObject(object_: Object) {
        instructions.add(Instruction(Simple.NEW_OBJECT, ObjectHolder(object_.fullName, object_.fields)))
    }

    fun newObject(fullName: String, vararg fields: NamedType) {
        instructions.add(Instruction(Simple.NEW_OBJECT, ObjectHolder(fullName, fields.toSet().toTypedArray())))
    }

    fun breakpoint() {
        instructions.add(getOrPut(Simple.BREAKPOINT))
    }

    fun dup() {
        instructions.add(getOrPut(Simple.DUP))
    }

    fun pop() {
        instructions.add(getOrPut(Simple.POP))
    }

    fun push(constant: Any) {
        instructions.add(Instruction(Simple.PUSH, constant))
    }

    fun sysCall(call: SysCommand) {
        instructions.add(Instruction(Simple.SYS_CALL, call))
    }

    private fun finalize(): Segment {
        //todo Validation and minor code merging of constant ops
        if (instructions.isEmpty() || instructions.last().opCode != Simple.RETURN) {
            return_()
        }

        validateStack(this, instructions) //todo make this segment based rather than array based
        val segment = MultiSegment.buildSegments(instructions)!!
        val stack = Stack<Type>()
        stack.push(Type.Constant.TInt(true))
        stack.push(Type.Constant.TByte(true))
        val vars: Array<Type?> = Array(localVars.size) {
            null
        }
        if (storage.args.isNotEmpty() && vars[0] == null && storage.args.isNotEmpty()) {
            storage.args.forEach {
                vars[localVars.indexOf(it.name)] = it.type
            }
        }
        segment.eval(storage, stack, vars)

        return segment
    }
}