package tech.poder.ir.data

import tech.poder.ir.commands.Command
import tech.poder.ir.commands.Simple
import tech.poder.ir.commands.SimpleValue
import tech.poder.ir.commands.SysCommand
import tech.poder.ir.data.base.Method
import tech.poder.ir.data.base.Object

data class CodeBuilder internal constructor(
    private val storage: Method,
    private val instructions: MutableList<Command> = mutableListOf()
) {
    //Bit Operations
    fun and() {
        instructions.add(Simple.AND)
    }

    fun or() {
        instructions.add(Simple.OR)
    }

    fun xor() {
        instructions.add(Simple.XOR)
    }

    fun unsignedShiftLeft() {
        instructions.add(Simple.SHL)
    }

    fun signedShiftLeft() {
        instructions.add(Simple.SAL)
    }

    fun signedShiftRight() {
        instructions.add(Simple.SAR)
    }

    fun unsignedShiftRight() {
        instructions.add(Simple.SHR)
    }

    fun rotateRight() {
        instructions.add(Simple.ROR)
    }

    fun rotateLeft() {
        instructions.add(Simple.ROL)
    }

    //Math
    fun inc() {
        instructions.add(Simple.INC)
    }

    fun add() {
        instructions.add(Simple.ADD)
    }

    fun mul() {
        instructions.add(Simple.MUL)
    }

    fun dec() {
        instructions.add(Simple.DEC)
    }

    fun sub() {
        instructions.add(Simple.SUB)
    }

    fun div() {
        instructions.add(Simple.DIV)
    }

    fun neg() {
        instructions.add(Simple.NEG)
    }

    //Labels
    private var lastLabel: UShort = 0u

    fun newLabel(): Label {
        return Label(lastLabel++)
    }

    fun jmp(to: Label) {
        instructions.add(SimpleValue.Jump(to))
    }

    fun placeLabel(label: Label) {
        label.use(instructions.size - 1)
    }

    //Local Vars
    fun setVar(name: String) {
        instructions.add(SimpleValue.SetVar(LocationRef.LocationByName(name)))
    }

    fun getVar(name: String) {
        instructions.add(SimpleValue.GetVar(LocationRef.LocationByName(name)))
    }

    //Array
    fun getArrayItem() {
        instructions.add(Simple.ARRAY_GET)
    }

    fun setArrayItem() {
        instructions.add(Simple.ARRAY_SET)
    }

    fun createArray(type: Type) {
        instructions.add(SimpleValue.ArrayCreate(type))
    }

    //Fields
    fun getField(name: String) {
        instructions.add(SimpleValue.GetField(LocationRef.LocationByName(name)))
    }

    fun setField(name: String) {
        instructions.add(SimpleValue.SetField(LocationRef.LocationByName(name)))
    }

    //Unsafe
    fun getUnsafeData(type: Type) {
        instructions.add(SimpleValue.UnsafeGet(type))
    }

    fun setUnsafeData(type: Type) {
        instructions.add(SimpleValue.UnsafeSet(type))
    }

    //Statements
    fun ifEquals(elseJump: Label) {
        instructions.add(SimpleValue.IfEquals(elseJump))
    }

    fun ifNotEquals(elseJump: Label) {
        instructions.add(SimpleValue.IfNotEquals(elseJump))
    }

    fun ifGreaterThan(elseJump: Label) {
        instructions.add(SimpleValue.IfGreaterThan(elseJump))
    }

    fun ifLessThan(elseJump: Label) {
        instructions.add(SimpleValue.IfLessThan(elseJump))
    }

    fun ifGreaterThanEqual(elseJump: Label) {
        instructions.add(SimpleValue.IfGreaterThanEquals(elseJump))
    }

    fun ifLessThanEqual(elseJump: Label) {
        instructions.add(SimpleValue.IfLessThanEquals(elseJump))
    }

    //Methods
    fun invokeMethod(method: Method) {
        invokeMethod(method.fullName)
    }

    fun invokeMethod(fullName: String) {
        instructions.add(
            SimpleValue.Invoke(LocationRef.LocationByName(fullName))
        )
    }

    fun launch(method: Method, priority: Int) {
        launch(method.fullName, priority)
    }

    fun launch(fullName: String, priority: Int) {
        instructions.add(SimpleValue.Launch(LocationRef.LocationByName("$fullName\u0000$priority")))
    }

    //Misc
    fun return_() {
        instructions.add(Simple.RETURN)
    }

    fun newObject(object_: Object) {
        newObject(object_.fullName)
    }

    fun newObject(fullName: String) {
        instructions.add(SimpleValue.NewObject(LocationRef.LocationByName(fullName)))
    }

    fun duplicate() {
        instructions.add(Simple.DUP)
    }

    fun pop() {
        instructions.add(Simple.POP)
    }

    fun push(constant: Byte) {
        instructions.add(SimpleValue.PushByte(constant))
    }

    fun push(constant: Short) {
        instructions.add(SimpleValue.PushShort(constant))
    }

    fun push(constant: Int) {
        instructions.add(SimpleValue.PushInt(constant))
    }

    fun push(constant: Long) {
        instructions.add(SimpleValue.PushLong(constant))
    }

    fun push(constant: Float) {
        instructions.add(SimpleValue.PushFloat(constant))
    }

    fun push(constant: Double) {
        instructions.add(SimpleValue.PushDouble(constant))
    }

    fun push(constant: UByte) {
        instructions.add(SimpleValue.PushUByte(constant))
    }

    fun push(constant: UShort) {
        instructions.add(SimpleValue.PushUShort(constant))
    }

    fun push(constant: UInt) {
        instructions.add(SimpleValue.PushUInt(constant))
    }

    fun push(constant: ULong) {
        instructions.add(SimpleValue.PushULong(constant))
    }

    fun push(constant: Char) {
        instructions.add(SimpleValue.PushChar(constant))
    }

    fun push(constant: CharSequence) {
        instructions.add(SimpleValue.PushChars(constant))
    }

    fun sysCall(call: SysCommand) {
        instructions.add(SimpleValue.SystemCall(call))
    }

    fun finalize() {
        TODO()
    }
}
