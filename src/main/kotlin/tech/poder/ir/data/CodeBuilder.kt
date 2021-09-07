package tech.poder.ir.data

import tech.poder.ir.data.base.Method
import tech.poder.ir.data.base.Object
import tech.poder.ir.data.base.Package
import tech.poder.ir.data.storage.Instruction
import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.data.storage.Type
import tech.poder.ir.data.storage.segment.MultiSegment
import tech.poder.ptir.metadata.Visibility
import kotlin.reflect.KClass

data class CodeBuilder(
    private val storage: Method,
    val instructions: MutableList<Instruction> = mutableListOf()
) {
    companion object {
        fun createMethod(
            package_: Package,
            name: String,
            vis: Visibility,
            returnType: KClass<out Type>? = null,
            args: Set<NamedType> = emptySet(),
            parent: Object? = null,
            block: (CodeBuilder) -> Unit
        ): Method {

            val method = Method(package_, parent, name, returnType, args, vis, 0, MultiSegment())
            val builder = CodeBuilder(method)

            block.invoke(builder)
            //method.instructions = builder.finalize()

            return method
        }

        /*private val constantMethods = mutableMapOf<Command, Instruction>()

        private fun getOrPut(command: Command): Instruction {
            return constantMethods.getOrPut(command) {
                Instruction(command)
            }
        }*/
    }

    private val localVars = storage.args.mapTo(mutableSetOf()) {
        it.name
    }

    val fullName by lazy {
        storage.fullName
    }

    //Bit Operations
    /*fun and() {
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
        localVars.add(name)
        instructions.add(Instruction(Simple.SET_VAR, localVars.indexOf(name)))
    }

    fun getVar(name: String) {

        check(name in localVars) {
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

    //Unsafe
    fun getUnsafeData(offset: Int, type: Type) {
        instructions.add(Instruction(Simple.UNSAFE_GET, OffsetType(offset, type)))
    }

    fun setUnsafeData(offset: Int, type: Type) {
        instructions.add(Instruction(Simple.UNSAFE_SET, OffsetType(offset, type)))
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
            Instruction(Simple.INVOKE_METHOD, MethodHolder(method.fullName, method.returnType, method.args))
        )
    }

    fun invokeMethod(fullName: String, returnType: KClass<out Type>? = null, vararg args: NamedType) {
        instructions.add(Instruction(Simple.INVOKE_METHOD, MethodHolder(fullName, returnType, args.toSet())))
    }

    fun unsignedUpscale() {
        instructions.add(Instruction(Simple.UNSIGNED_UPSCALE))
    }

    fun launch(method: Method, priority: Int) {

        checkNotNull(method.returnType) {
            "Launched methods cannot have a return type!"
        }

        launch(method.fullName, priority, *method.args.toTypedArray())
    }

    fun launch(fullName: String, priority: Int, vararg args: NamedType) {
        instructions.add(Instruction(Simple.LAUNCH, Pair(priority, MethodHolder(fullName, null, args.toSet()))))
    }

    //Misc
    fun return_() {
        instructions.add(getOrPut(Simple.RETURN))
    }

    fun newObject(object_: Object) {
        instructions.add(Instruction(Simple.NEW_OBJECT, ObjectHolder(object_.fullName, object_.fields)))
    }

    fun newObject(fullName: String, vararg fields: NamedType) {
        instructions.add(Instruction(Simple.NEW_OBJECT, ObjectHolder(fullName, fields.toSet().toList())))
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

        // TODO: Validation and minor code merging of constant ops
        if (instructions.isEmpty() || instructions.last().opCode != Simple.RETURN) {
            return_()
        }

        val segment = MultiSegment.buildSegments(instructions)!!
        val stack = Stack<Type>()

        val vars = MutableList<KClass<out Type>?>(localVars.size) {
            null
        }

        if (storage.args.isNotEmpty() && vars[0] == null && storage.args.isNotEmpty()) {
            storage.args.forEach {
                vars[localVars.indexOf(it.name)] = it.type
            }
        }

        val labels = mutableMapOf<Int, Label>()
        instructions.forEachIndexed { i, instruction ->
            if (instruction.extra is Label) {
                labels[i] = instruction.extra as Label
            }
        }
        segment.eval(storage, stack, vars, 0, labels)

        return segment
    }*/
}