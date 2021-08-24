package tech.poder.ir.instructions.simple

import tech.poder.ir.instructions.common.Instruction
import tech.poder.ir.instructions.common.Method
import tech.poder.ir.instructions.common.Object
import tech.poder.ir.instructions.common.special.Label
import tech.poder.ir.instructions.common.special.SpecialCalls
import tech.poder.ir.instructions.complex.Complex
import java.util.*

class CodeBuilder(val returnItem: Boolean) {
    private val base = ArrayList<Instruction>()

    fun setVar(name: String) {
        base.add(Instruction.create(Simple.STORE_VAR, name))
    }

    fun getVar(name: String) {
        base.add(Instruction.create(Simple.GET_VAR, name))
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

    fun new(obj: Object) {
        base.add(Instruction.create(Simple.NEW_OBJECT, obj))
        base.add(Instruction(Complex.INCREMENT_REFERENCE))
    }

    fun invoke(method: Method) {
        invoke(method.parent.nameSpace + "." + method.name, method.argCount.toInt())
    }

    fun invoke(name: String, args: Int) {
        base.add(
            Instruction.create(
                Simple.INVOKE_METHOD,
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

    fun suspend() {
        base.add(Instruction(Simple.SUSPEND))
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
        validateStack()
        return base
    }

    private fun validateStack() {
        val stack = Stack<Any>()

        //todo validate stack

        if (returnItem) {
            //todo check that stack has 1 item left
        }
    }
}