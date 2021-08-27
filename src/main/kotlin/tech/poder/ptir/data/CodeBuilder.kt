package tech.poder.ptir.data

import tech.poder.ptir.commands.Command
import tech.poder.ptir.commands.Simple
import tech.poder.ptir.commands.SysCommand
import tech.poder.ptir.data.base.Method
import tech.poder.ptir.data.base.Object
import tech.poder.ptir.data.base.Package
import tech.poder.ptir.data.storage.*
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

data class CodeBuilder(
    private val storage: Method,
    val instructions: ArrayList<Instruction> = arrayListOf()
) {
    companion object {
        fun createMethod(
            package_: Package,
            name: String,
            returnType: Type? = null,
            args: Set<NamedType> = emptySet(),
            parent: Object? = null,
            block: (CodeBuilder) -> Unit
        ): Method {
            val method = Method(package_, parent, name, returnType, args, emptyArray())

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

        private fun validateStack(builder: CodeBuilder, instructions: ArrayList<Instruction>): Stack<Type> {
            val stack = Stack<Type>()
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
                    Simple.INC -> {
                        val popped = safePop(stack, "INC")
                        check(popped is Type.Constant && popped !is Type.Constant.TString) {
                            "INC called on illegal type: $popped!"
                        }
                        if (popped.constant) {
                            val prev = instructions[index - 1]
                            prev.extra = addNumbers(prev.extra as Number, toSameNumbers(1, prev.extra as Number))
                            instructions.removeAt(index)
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
                            instructions.removeAt(index)
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
                                instructions.removeAt(index)
                                instructions.removeAt(index - 1)
                                index -= 2
                                toLarger(poppedA, poppedB)
                            } else {
                                if (poppedB.constant && (prevB.extra as Number).toDouble() == 1.0) {
                                    instructions.removeAt(index - 1)
                                    instruction.opCode = Simple.DEC
                                }
                                if (poppedA.constant && (prevA.extra as Number).toDouble() == 1.0) {
                                    instructions.removeAt(index - 2)
                                    instruction.opCode = Simple.DEC
                                }
                                val tmp = toLarger(poppedA, poppedB)
                                tmp.constant = false
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
                                instructions.removeAt(index)
                                instructions.removeAt(index - 1)
                                index -= 2
                                toLarger(poppedA, poppedB)
                            } else {
                                if (poppedB.constant && (prevB.extra as Number).toDouble() == 1.0) {
                                    instructions.removeAt(index - 1)
                                    instruction.opCode = Simple.INC
                                }
                                if (poppedA.constant && (prevA.extra as Number).toDouble() == 1.0) {
                                    instructions.removeAt(index - 2)
                                    instruction.opCode = Simple.INC
                                }
                                val tmp = toLarger(poppedA, poppedB)
                                tmp.constant = false
                                tmp
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
                                instructions.removeAt(index)
                                instructions.removeAt(index - 1)
                                index -= 2
                                toLarger(poppedA, poppedB)
                            } else {
                                val tmp = toLarger(poppedA, poppedB)
                                tmp.constant = false
                                tmp
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
                                instructions.removeAt(index)
                                instructions.removeAt(index - 1)
                                index -= 2
                                toLarger(poppedA, poppedB)
                            } else {
                                val tmp = toLarger(poppedA, poppedB)
                                tmp.constant = false
                                tmp
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
                                instructions.removeAt(index)
                                instructions.removeAt(index - 1)
                                index -= 2
                                toLarger(poppedA, poppedB)
                            } else {
                                val tmp = toLarger(poppedA, poppedB)
                                tmp.constant = false
                                tmp
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
                                prevA.extra = shrNumbers(prevA.extra as Number, prevB.extra as Int)
                                instructions.removeAt(index)
                                instructions.removeAt(index - 1)
                                index -= 2
                                toLarger(poppedA, poppedB)
                            } else {
                                val tmp = toLarger(poppedA, poppedB)
                                tmp.constant = false
                                tmp
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
                                prevA.extra = shlNumbers(prevA.extra as Number, prevB.extra as Int)
                                instructions.removeAt(index)
                                instructions.removeAt(index - 1)
                                index -= 2
                                toLarger(poppedA, poppedB)
                            } else {
                                val tmp = toLarger(poppedA, poppedB)
                                tmp.constant = false
                                tmp
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
                                prevA.extra = ushrNumbers(prevA.extra as Number, prevB.extra as Int)
                                instructions.removeAt(index)
                                instructions.removeAt(index - 1)
                                index -= 2
                                toLarger(poppedA, poppedB)
                            } else {
                                val tmp = toLarger(poppedA, poppedB)
                                tmp.constant = false
                                tmp
                            }
                        )
                    }
                    else -> error("Unknown command: ${instruction.opCode}")
                }
                index++
            }
            return stack
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

        private fun safePop(stack: Stack<Type>, message: String): Type {
            check(stack.isNotEmpty()) {
                "$message could not be executed because stack was empty!"
            }
            return stack.pop()
        }

        private fun toLarger(a: Number, b: Number): Number {
            return when {
                a is Double || b is Double -> a.toDouble()
                a is Float || b is Float -> a.toFloat()
                a is Long || b is Long -> a.toLong()
                a is Int || b is Int -> a.toInt()
                a is Short || b is Short -> a.toShort()
                else -> a.toByte()
            }
        }

        private fun toLarger(a: Type.Constant, b: Type.Constant): Type.Constant {
            return when {
                a is Type.Constant.TDouble || b is Type.Constant.TDouble -> Type.Constant.TDouble(true)
                a is Type.Constant.TFloat || b is Type.Constant.TFloat -> Type.Constant.TFloat(true)
                a is Type.Constant.TLong || b is Type.Constant.TLong -> Type.Constant.TLong(true)
                a is Type.Constant.TInt || b is Type.Constant.TInt -> Type.Constant.TInt(true)
                a is Type.Constant.TShort || b is Type.Constant.TShort -> Type.Constant.TShort(true)
                else -> Type.Constant.TByte(true)
            }
        }

        private fun toSameNumbers(from: Number, to: Number): Number {
            return when (to) {
                is Double -> from.toDouble()
                is Float -> from.toFloat()
                is Long -> from.toLong()
                is Int -> from.toInt()
                is Short -> from.toShort()
                else -> from.toByte()
            }
        }

        private fun addNumbers(a: Number, b: Number): Number {
            return when (toLarger(a, b)) {
                is Double -> a.toDouble() + b.toDouble()
                is Float -> a.toFloat() + b.toFloat()
                is Long -> a.toLong() + b.toLong()
                is Int -> a.toInt() + b.toInt()
                is Short -> a.toShort() + b.toShort()
                else -> a.toByte() + b.toByte()
            }
        }

        private fun orNumbers(a: Number, b: Number): Number {
            return when (toLarger(a, b)) {
                is Long -> a.toLong() or b.toLong()
                is Int -> a.toInt() or b.toInt()
                is Short -> a.toShort() or b.toShort()
                else -> a.toByte() or b.toByte()
            }
        }

        private fun xorNumbers(a: Number, b: Number): Number {
            return when (toLarger(a, b)) {
                is Long -> a.toLong() xor b.toLong()
                is Int -> a.toInt() xor b.toInt()
                is Short -> a.toShort() xor b.toShort()
                else -> a.toByte() xor b.toByte()
            }
        }

        private fun andNumbers(a: Number, b: Number): Number {
            return when (toLarger(a, b)) {
                is Long -> a.toLong() and b.toLong()
                is Int -> a.toInt() and b.toInt()
                is Short -> a.toShort() and b.toShort()
                else -> a.toByte() and b.toByte()
            }
        }

        private fun shlNumbers(a: Number, b: Int): Number {
            return when (a) {
                is Long -> a.toLong() shl b
                is Int -> a.toInt() shl b
                is Short -> (a.toInt() shl b).toShort()
                else -> (a.toInt() shl b).toByte()
            }
        }

        private fun shrNumbers(a: Number, b: Int): Number {
            return when (a) {
                is Long -> a.toLong() shr b
                is Int -> a.toInt() shr b
                is Short -> (a.toInt() shr b).toShort()
                else -> (a.toInt() shr b).toByte()
            }
        }

        private fun ushrNumbers(a: Number, b: Int): Number {
            return when (a) {
                is Long -> a.toLong() ushr b
                is Int -> a.toInt() ushr b
                is Short -> (a.toInt() ushr b).toShort()
                else -> (a.toInt() ushr b).toByte()
            }
        }

        private fun subNumbers(a: Number, b: Number): Number {
            return when (toLarger(a, b)) {
                is Double -> a.toDouble() - b.toDouble()
                is Float -> a.toFloat() - b.toFloat()
                is Long -> a.toLong() - b.toLong()
                is Int -> a.toInt() - b.toInt()
                is Short -> a.toShort() - b.toShort()
                else -> a.toByte() - b.toByte()
            }
        }
    }

    private var localVars = ArrayList<String>()

    init {
        if (storage.args.isNotEmpty()) {
            localVars.add("args")
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

    //Arguments
    private fun indexOfArg(name: String): Int {
        val index = storage.args.indexOfFirst { it.name == name }
        check(index > -1) {
            "Arg $name does not exist!\n\tKnown Args:\n\t\t${storage.args.joinToString("\n\t\t")}"
        }
        return index
    }

    fun getArg(name: String) {
        val index = indexOfArg(name)
        push(index)
        getVar("args")
        getArrayItem()
    }

    //Array
    fun getArrayItem() {
        instructions.add(getOrPut(Simple.ARRAY_GET))
    }

    fun setArrayItem() {
        instructions.add(getOrPut(Simple.ARRAY_SET))
    }

    fun createArray(types: Array<Type>) {
        instructions.add(Instruction(Simple.ARRAY_CREATE, types))
    }

    //Fields
    private fun indexOfField(name: String): Int {
        check(storage.parent != null) {
            "Floating method has no fields!"
        }
        val index = storage.parent.fields.indexOfFirst { it.name == name }
        check(index > -1) {
            "Field $name does not exist!\n\tKnown Fields:\n\t\t${storage.parent.fields.joinToString("\n\t\t")}"
        }
        return index
    }

    fun getField(name: String) {
        val index = indexOfField(name)
        instructions.add(Instruction(Simple.GET_FIELD, index))
    }

    fun setField(name: String) {
        val index = indexOfField(name)
        instructions.add(Instruction(Simple.SET_FIELD, index))
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

    fun finalize(): Array<Instruction> {
        //todo Validation and minor code merging of constant ops
        if (instructions.isEmpty() || instructions.last().opCode != Simple.RETURN) {
            return_()
        }

        validateStack(this, instructions)

        return instructions.toTypedArray()
    }
}