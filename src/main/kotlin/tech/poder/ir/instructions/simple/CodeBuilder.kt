package tech.poder.ir.instructions.simple

import tech.poder.ir.instructions.common.Instruction
import tech.poder.ir.instructions.common.Method
import tech.poder.ir.instructions.common.Object
import tech.poder.ir.instructions.common.special.Label
import tech.poder.ir.instructions.common.special.SpecialCalls
import tech.poder.ir.instructions.complex.Complex

class CodeBuilder(val returnItem: Boolean, val nameSpace: String) {
    private val manualArgs = mutableMapOf<String, UByte>()
    private val base = ArrayList<Instruction>()

    fun setVar(name: String) {
        base.add(Instruction.create(Simple.STORE_VAR, name))
    }

    fun idArgs(vararg names: String) {
        names.reversed().forEach {
            manualArgs[it] = (manualArgs.size).toUByte()
        }
    }

    fun getArg(name: String) {
        push(manualArgs[name]!!.toInt())
        getVar("args")
        getArrayItem()
    }

    fun and() {
        base.add(Instruction(Simple.AND))
    }

    fun or() {
        base.add(Instruction(Simple.OR))
    }

    fun xor() {
        base.add(Instruction(Simple.XOR))
    }

    fun unsignedShiftLeft() {
        base.add(Instruction(Simple.SHL))
    }

    fun signedShiftLeft() {
        base.add(Instruction(Simple.SAL))
    }

    fun signedShiftRight() {
        base.add(Instruction(Simple.SAR))
    }

    fun unsignedShiftRight() {
        base.add(Instruction(Simple.SHR))
    }

    fun rotateRight() {
        base.add(Instruction(Simple.ROR))
    }

    fun rotateLeft() {
        base.add(Instruction(Simple.ROL))
    }

    fun getVar(name: String) {
        base.add(Instruction.create(Simple.GET_VAR, name))
    }

    fun createArray() {
        base.add(Instruction(Simple.ARRAY_CREATE))
    }

    fun getArrayItem() {
        base.add(Instruction(Simple.ARRAY_GET))
    }

    fun setArrayItem() {
        base.add(Instruction(Simple.ARRAY_SET))
    }

    fun push(constant: Any) {
        base.add(Instruction.create(Simple.PUSH, constant))
    }

    fun sysCall(call: SpecialCalls, vararg args: Any) {
        base.add(Instruction.create(Complex.SYS_CALL, call, *args))
    }

    fun getLocalField(name: String) {
        base.add(Instruction.create(Simple.GET_FIELD, "$nameSpace$name"))
    }

    fun setLocalField(name: String) {
        base.add(Instruction.create(Simple.SET_FIELD, "$nameSpace$name"))
    }

    fun getField(name: String) {
        base.add(Instruction.create(Simple.GET_FIELD, name))
    }

    fun setField(name: String) {
        base.add(Instruction.create(Simple.SET_FIELD, name))
    }

    fun new(obj: Object) {
        new(obj.nameSpace)
    }

    fun new(objNameSpace: String) {
        base.add(Instruction.create(Simple.NEW_OBJECT, objNameSpace))
    }

    fun invoke(method: Method) {
        invoke(method.fullName, method.argCount.toInt(), method.returns)
    }

    fun invoke(name: String, args: Int, returns: Boolean) {
        base.add(
            Instruction.create(
                Simple.INVOKE_METHOD,
                name,
                args,
                returns
            )
        )
    }

    fun suspend() {
        base.add(Instruction(Simple.SUSPEND))
    }

    fun launch(method: Method) {
        launch(method.fullName, method.argCount.toInt())
    }

    fun launch(name: String, args: Int) {
        base.add(
            Instruction.create(
                Simple.LAUNCH,
                name,
                args
            )
        )
    }

    fun compare() {//compare is int on 64 bit... I think
        base.add(Instruction(Complex.COMPARE))
    }

    private var last: UShort = 0u

    fun newLabel(): Label {
        return Label(last++)
    }

    fun ifEquals(elseJump: Label) {
        base.add(Instruction.create(Simple.IF_EQ, elseJump)) //Compare gives 0 for equal
    }

    fun ifNotEquals(elseJump: Label) {
        base.add(Instruction.create(Simple.IF_NEQ, elseJump))
    }

    fun ifGreaterThan(elseJump: Label) {
        base.add(Instruction.create(Simple.IF_GT, elseJump))
    }

    fun ifLessThan(elseJump: Label) {
        base.add(Instruction.create(Simple.IF_LT, elseJump))
    }

    fun ifGreaterThanEqual(elseJump: Label) {
        base.add(Instruction.create(Simple.IF_GT_EQ, elseJump))
    }

    fun jmp(to: Label) {
        base.add(Instruction.create(Simple.JMP, to))
    }

    fun placeLabel(label: Label) {
        label.use(base.size - 1)
    }

    fun return_() {
        base.add(Instruction(Simple.RETURN))
    }

    fun inc() {
        base.add(Instruction(Simple.INC))
    }

    fun add() {
        base.add(Instruction(Simple.ADD))
    }

    fun mul() {
        base.add(Instruction(Simple.MUL))
    }

    fun dec() {
        base.add(Instruction(Simple.DEC))
    }

    fun sub() {
        base.add(Instruction(Simple.SUB))
    }

    fun div() {
        base.add(Instruction(Simple.DIV))
    }

    fun neg() {
        base.add(Instruction(Simple.NEG))
    }

    fun ifLessThanEqual(elseJump: Label) {
        base.add(Instruction.create(Simple.IF_LT_EQ, elseJump))
    }

    fun pop() {
        base.add(Instruction(Simple.POP))
    }

    fun dup() {
        base.add(Instruction(Simple.DUP))
    }

    fun breakpoint() {
        base.add(Instruction(Simple.BREAKPOINT))
    }

    fun code(): ArrayList<Instruction> {
        if (base.isEmpty()) {
            return_()
        }
        if (base.last().opcode != Simple.RETURN) {
            return_()
        }
        val unmatched = mutableListOf<UShort>()
        val dictionary = mutableMapOf<String, UByte>()
        var newId: UByte = 2u
        dictionary["object"] = 0u
        dictionary["args"] = 1u
        base.forEachIndexed { index, instruction ->
            when (instruction.opcode) {
                Simple.STORE_VAR, Simple.GET_VAR -> {
                    val key = instruction.extra.first() as String
                    if (instruction.opcode == Simple.GET_VAR && !dictionary.containsKey(key)) {
                        throw IllegalStateException(
                            "GetVar done before StoreVar!\nStackTrace:\n\tIndex: $index\n\tVariable: $key\n\tFullProgram:\n\t\t${
                                base.joinToString(
                                    "\n\t\t"
                                )
                            }"
                        )
                    }
                    instruction.extra[0] = dictionary.getOrPut(key) {
                        newId++
                    }
                }
            }
            if (instruction.extra.isNotEmpty()) {
                val potentialLabel = instruction.extra.first()
                if (potentialLabel is Label) {
                    if (!potentialLabel.isUsed()) {
                        unmatched.add(potentialLabel.id)
                    }
                    instruction.extra[0] = potentialLabel.offset - index
                }
            }
        }

        check(unmatched.isEmpty()) {
            "Label(s) Mismatched: ${unmatched.joinToString(", ")}"
        }

        //todo validate stack
        return base
    }
}