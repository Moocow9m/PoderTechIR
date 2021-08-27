package tech.poder.ptir.data

import tech.poder.ptir.commands.Command
import tech.poder.ptir.commands.Simple
import tech.poder.ptir.commands.SysCommand
import tech.poder.ptir.data.base.Method
import tech.poder.ptir.data.base.Object
import tech.poder.ptir.data.base.Package
import tech.poder.ptir.data.storage.*
import java.util.*

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
    fun ifEquals(elseJump: Label) {
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

    //Misc
    fun return_() {
        instructions.add(getOrPut(Simple.RETURN))
    }

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

        val stack = Stack<Type>()



        return instructions.toTypedArray()
    }
}