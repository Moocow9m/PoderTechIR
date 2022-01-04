package tech.poder.ir.data

import tech.poder.ir.commands.*
import tech.poder.ir.data.base.unlinked.UnlinkedMethod
import tech.poder.ir.data.base.unlinked.UnlinkedObject
import tech.poder.ir.data.storage.segment.MetaSegment

data class CodeBuilder internal constructor(
	private val storage: UnlinkedMethod,
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
		instructions.add(SimpleValue.IfType.IfEquals(elseJump))
	}

	fun ifNotEquals(elseJump: Label) {
		instructions.add(SimpleValue.IfType.IfNotEquals(elseJump))
	}

	fun ifGreaterThan(elseJump: Label) {
		instructions.add(SimpleValue.IfType.IfGreaterThan(elseJump))
	}

	fun ifLessThan(elseJump: Label) {
		instructions.add(SimpleValue.IfType.IfLessThan(elseJump))
	}

	fun ifGreaterThanEqual(elseJump: Label) {
		instructions.add(SimpleValue.IfType.IfGreaterThanEquals(elseJump))
	}

	fun ifLessThanEqual(elseJump: Label) {
		instructions.add(SimpleValue.IfType.IfLessThanEquals(elseJump))
	}

	//Methods
	fun invokeMethod(method: UnlinkedMethod) {
		invokeMethod(method.fullName)
	}

	fun invokeMethod(fullName: String) {
		instructions.add(
			SimpleValue.Invoke(LocationRef.LocationByName(fullName))
		)
	}

	fun launch(method: UnlinkedMethod, priority: Int) {
		launch(method.fullName, priority)
	}

	fun launch(fullName: String, priority: Int) {
		instructions.add(SimpleValue.Launch(LocationRef.LocationByName("$fullName\u0000$priority")))
	}

	//Misc
	fun return_() {
		instructions.add(Simple.RETURN)
	}

	fun newObject(object_: UnlinkedObject) {
		newObject(object_.fullName)
	}

	fun newObject(fullName: String) {
		instructions.add(SimpleValue.NewObject(LocationRef.LocationByName(fullName))) //todo on resolution make this a struct. merge duplicate type structs even if name differs
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

	fun markLineNumber(line: UInt) {
		instructions.add(DebugValue.LineNumber(line))
	}

	fun writeLineSource(line: CharSequence) {
		instructions.add(DebugValue.Line(line))
	}

	fun finalize() {
		val replaceInstructions = mutableMapOf<Int, Command>()
		instructions.forEachIndexed { index, command ->
			when (command) {
				is SimpleValue.Jump -> {
					replaceInstructions[index] = SimpleValue.JumpShort(command.data.location - index)
				}
				is SimpleValue.IfType -> {
					replaceInstructions[index] = when (command) {
						is SimpleValue.IfType.IfEquals -> {
							SimpleValue.IfTypeShort.IfEquals(command.data.location - index)
						}
						is SimpleValue.IfType.IfNotEquals -> {
							SimpleValue.IfTypeShort.IfNotEquals(command.data.location - index)
						}
						is SimpleValue.IfType.IfGreaterThan -> {
							SimpleValue.IfTypeShort.IfGreaterThan(command.data.location - index)
						}
						is SimpleValue.IfType.IfLessThan -> {
							SimpleValue.IfTypeShort.IfLessThan(command.data.location - index)
						}
						is SimpleValue.IfType.IfGreaterThanEquals -> {
							SimpleValue.IfTypeShort.IfGreaterThanEquals(command.data.location - index)
						}
						is SimpleValue.IfType.IfLessThanEquals -> {
							SimpleValue.IfTypeShort.IfLessThanEquals(command.data.location - index)
						}
					}
				}
			}
		}
		replaceInstructions.forEach { (t, u) ->
			instructions[t] = u
		}
		storage.instructions = MetaSegment.buildControlGraph(instructions)//MultiSegment.buildSegments(instructions)
	}
}
