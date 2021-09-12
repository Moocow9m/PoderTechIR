package tech.poder.ir.data.storage.segment

import tech.poder.ir.commands.Command
import tech.poder.ir.commands.DebugValue
import tech.poder.ir.commands.Simple
import tech.poder.ir.commands.SimpleValue
import tech.poder.ir.data.LocationRef
import tech.poder.ir.data.Type
import tech.poder.ir.data.base.api.APIContainer
import tech.poder.ir.data.base.api.PublicMethod
import tech.poder.ir.data.base.api.PublicObject
import tech.poder.ir.data.base.unlinked.UnlinkedContainer
import tech.poder.ir.data.base.unlinked.UnlinkedMethod
import tech.poder.ir.data.base.unlinked.UnlinkedObject
import tech.poder.ir.metadata.IdMethod
import tech.poder.ir.metadata.IdObject
import tech.poder.ir.metadata.IdType
import tech.poder.ir.metadata.NameId
import tech.poder.ir.util.MemorySegmentBuffer
import java.util.*
import kotlin.math.ceil

@JvmInline
value class SegmentPart(
    val instructions: MutableList<Command> = mutableListOf()
) : Segment {

    companion object {
        private fun safePop(stack: Stack<Type>, message: String): Type {

            check(stack.isNotEmpty()) {
                "$message could not be executed because stack was empty!"
            }

            return stack.pop()
        }

        private fun toLargerType(a: Type.Primitive.Numeric, b: Type.Primitive.Numeric): Type {
            return when {
                a is Type.Primitive.Numeric.FloatingPoint.Double || b is Type.Primitive.Numeric.FloatingPoint.Double -> Type.Primitive.Numeric.FloatingPoint.Double
                a is Type.Primitive.Numeric.FloatingPoint.Float || b is Type.Primitive.Numeric.FloatingPoint.Float -> Type.Primitive.Numeric.FloatingPoint.Float
                a is Type.Primitive.Numeric.Basic.Long || b is Type.Primitive.Numeric.Basic.Long -> Type.Primitive.Numeric.Basic.Long
                a is Type.Primitive.Numeric.Basic.Int || b is Type.Primitive.Numeric.Basic.Int -> Type.Primitive.Numeric.Basic.Int
                a is Type.Primitive.Numeric.Basic.Short || b is Type.Primitive.Numeric.Basic.Short -> Type.Primitive.Numeric.Basic.Short
                else -> Type.Primitive.Numeric.Basic.Byte
            }
        }
    }

    override fun eval(
        dependencies: Set<APIContainer>,
        self: UnlinkedContainer,
        method: UnlinkedMethod,
        stack: Stack<Type>,
        currentIndex: Int,
        vars: MutableMap<CharSequence, UInt>,
        type: MutableMap<UInt, Type>,
        depMap: List<NameId>
    ): Int {
        var index = currentIndex
        var lastDebugLine: CharSequence = ""
        var lastDebugNumber = 0u
        val indexOffset = currentIndex
        val replaceList = mutableMapOf<Int, Command>()
        instructions.forEach { command ->
            when (command) {
                Simple.RETURN -> {
                    if (method.returnType != Type.Unit) {
                        val ret = safePop(stack, "RETURN")
                        check(ret == method.returnType) {
                            "Return type error: ${
                                processDebug(
                                    lastDebugNumber,
                                    lastDebugLine
                                )
                            }Wanted: ${method.returnType::class} Got: ${ret::class}"
                        }
                    }
                    check(stack.isEmpty()) {
                        "Stack wasn't empty! ${
                            processDebug(
                                lastDebugNumber,
                                lastDebugLine
                            )
                        }\n\tStack:\n\t\t${stack.joinToString("\n\t\t")}"
                    }
                }
                Simple.POP -> safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}POP")
                Simple.DUP -> stack.push(stack.peek())
                Simple.DEC, Simple.INC, Simple.NEG -> {
                    val a = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}${(command as Simple).name}")
                    check(a is Type.Primitive.Numeric) {
                        "${a::class} is not numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    stack.push(a)
                }
                Simple.ADD -> {
                    val b = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ADD")
                    val a = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ADD")
                    check(a is Type.Primitive) {
                        "${a::class} is not numeric or char-based! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    check(b is Type.Primitive) {
                        "${b::class} is not numeric or char-based! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    if (a is Type.Primitive.CharBased || b is Type.Primitive.CharBased) {
                        stack.push(Type.Primitive.CharBased.String)
                    } else {
                        stack.push(toLargerType(a as Type.Primitive.Numeric, b as Type.Primitive.Numeric))
                    }
                }
                Simple.SUB, Simple.MUL, Simple.DIV,
                Simple.XOR, Simple.SAL, Simple.SAR,
                Simple.SHR, Simple.SHL, Simple.AND,
                Simple.OR, Simple.ROL, Simple.ROR
                -> {
                    val b = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}${(command as Simple).name}")
                    val a = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}${command.name}")
                    check(a is Type.Primitive.Numeric) {
                        "${a::class} is not numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    check(b is Type.Primitive.Numeric) {
                        "${b::class} is not numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    stack.push(toLargerType(a, b))
                }
                Simple.ARRAY_SET -> {
                    val value = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ARRAY_SET")
                    val aIndex = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ARRAY_SET")
                    val array = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ARRAY_SET")
                    check(array is Type.ArrayType) {
                        "${array::class} is not an array! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }

                    check(aIndex is Type.Primitive.Numeric.Basic) {
                        "${aIndex::class} is not a basic numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }

                    if (array is Type.ArrayType.RuntimeArray) {
                        check(value == array.type) {
                            "Array type error: ${value::class} != ${array.type::class} ${
                                processDebug(
                                    lastDebugNumber,
                                    lastDebugLine
                                )
                            }"
                        }
                    } else {
                        array as Type.ArrayType.Array
                        check(value == array.type) {
                            "Array type error: ${value::class} != ${array.type::class} ${
                                processDebug(
                                    lastDebugNumber,
                                    lastDebugLine
                                )
                            }"
                        }
                        check(index < array.size) {
                            "Array size error: $index >= ${array.size} ${processDebug(lastDebugNumber, lastDebugLine)}"
                        }
                    }
                }
                Simple.ARRAY_GET -> {
                    val aIndex = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ARRAY_GET")
                    val array = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ARRAY_GET")
                    check(array is Type.ArrayType) {
                        "${array::class} is not an array! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }

                    check(aIndex is Type.Primitive.Numeric.Basic) {
                        "${aIndex::class} is not a basic numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }

                    if (array is Type.ArrayType.RuntimeArray) {
                        stack.push(array.type)
                    } else {
                        array as Type.ArrayType.Array
                        stack.push(array.type)
                    }
                }
                is DebugValue.LineNumber -> lastDebugNumber = command.line
                is DebugValue.Line -> lastDebugLine = command.line
                is SimpleValue.PushByte -> {
                    stack.push(Type.Primitive.Numeric.Basic.Byte)
                }
                is SimpleValue.PushShort -> {
                    stack.push(Type.Primitive.Numeric.Basic.Short)
                }
                is SimpleValue.PushInt -> {
                    stack.push(Type.Primitive.Numeric.Basic.Int)
                }
                is SimpleValue.PushLong -> {
                    stack.push(Type.Primitive.Numeric.Basic.Long)
                }
                is SimpleValue.PushUByte -> {
                    stack.push(Type.Primitive.Numeric.Basic.UByte)
                }
                is SimpleValue.PushUShort -> {
                    stack.push(Type.Primitive.Numeric.Basic.UShort)
                }
                is SimpleValue.PushUInt -> {
                    stack.push(Type.Primitive.Numeric.Basic.UInt)
                }
                is SimpleValue.PushULong -> {
                    stack.push(Type.Primitive.Numeric.Basic.ULong)
                }
                is SimpleValue.PushFloat -> {
                    stack.push(Type.Primitive.Numeric.FloatingPoint.Float)
                }
                is SimpleValue.PushDouble -> {
                    stack.push(Type.Primitive.Numeric.FloatingPoint.Double)
                }
                is SimpleValue.PushChar -> {
                    stack.push(Type.Primitive.CharBased.Char)
                }
                is SimpleValue.PushChars -> {
                    stack.push(Type.Primitive.CharBased.String)
                }
                is SimpleValue.GetVar -> {
                    stack.push(when (command.data) {
                        is LocationRef.LocationByID -> {
                            type[command.data.id]
                        }
                        is LocationRef.LocationByName -> {
                            val id = vars[command.data.name]
                            check(id != null) {
                                "Var: ${command.data.name} does not exist! ${
                                    processDebug(
                                        lastDebugNumber,
                                        lastDebugLine
                                    )
                                }"
                            }
                            replaceList[index - indexOffset] = SimpleValue.GetVar(LocationRef.LocationByID(id))
                            type[id]
                        }
                        else -> error("SegFault") //should never happen
                    })
                }
                is SimpleValue.SetVar -> {
                    val t = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}SetVar")
                    when (command.data) {
                        is LocationRef.LocationByID -> {
                            type[command.data.id] = t
                        }
                        is LocationRef.LocationByName -> {
                            if (!vars.containsKey(command.data.name)) {
                                vars[command.data.name] = vars.size.toUInt()
                            }
                            val id = vars[command.data.name]!!
                            replaceList[index - indexOffset] = SimpleValue.SetVar(LocationRef.LocationByID(id))
                            type[id] = t
                        }
                    }
                }
                is SimpleValue.ArrayCreate -> {
                    val size = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}ArrayCreate")
                    check(size is Type.Primitive.Numeric.Basic) {
                        "${size::class} is not basic numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    stack.push(Type.ArrayType.RuntimeArray(command.data))
                }
                is SimpleValue.SystemCall -> {
                    val call = command.data
                    call.args.forEach {
                        val popped = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}SystemCall")
                        check(popped == it) {
                            "SysCall type check error! ${
                                processDebug(
                                    lastDebugNumber,
                                    lastDebugLine
                                )
                            }Wanted: ${it::class} Got: ${popped::class}"
                        }
                    }
                }
                is SimpleValue.IfTypeShort -> {
                    val b = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}${command::class}")
                    val a = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}${command::class}")
                    check(a is Type.Primitive.Numeric) {
                        "${a::class} is not numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    check(b is Type.Primitive.Numeric) {
                        "${b::class} is not numeric! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                }
                is SimpleValue.JumpShort -> {
                    //No stack change
                }
                is SimpleValue.GetField -> {
                    val obj = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}GET_FIELD")
                    check(obj is Type.ObjRef) {
                        "${obj::class} is not an objRef! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    val fieldId = if (command.data is LocationRef.LocationByName) {
                        val id = resolveDepField(
                            self, dependencies, depMap,
                            obj.id,
                            command.data.name.toString()
                        )
                        replaceList[index - indexOffset] =
                            SimpleValue.GetField(LocationRef.LocationByID(id.id))
                        id
                    } else {
                        command.data as LocationRef.LocationByID
                        if (command.data.id == 0u) {
                            IdType(
                                command.data.id,
                                self.locateField(obj.id.id.second, command.data.id)
                            )
                        } else {
                            val name = depMap.first { it.id == obj.id.id.first }.name
                            IdType(
                                command.data.id,
                                dependencies.first { it.name == name }.locateField(obj.id.id.second, command.data.id)
                            )
                        }
                    }
                    stack.push(fieldId.type)
                }
                is SimpleValue.SetField -> {
                    val data = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}SET_FIELD")
                    val obj = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}SET_FIELD")
                    check(obj is Type.ObjRef) {
                        "${obj::class} is not an objRef! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                    val fieldId = if (command.data is LocationRef.LocationByName) {
                        val id = resolveDepField(
                            self, dependencies, depMap,
                            obj.id,
                            command.data.name.toString()
                        )
                        replaceList[index - indexOffset] =
                            SimpleValue.GetField(LocationRef.LocationByID(id.id))
                        id
                    } else {
                        command.data as LocationRef.LocationByID
                        if (command.data.id == 0u) {
                            IdType(
                                command.data.id,
                                self.locateField(obj.id.id.second, command.data.id)
                            )
                        } else {
                            val name = depMap.first { it.id == obj.id.id.first }.name
                            IdType(
                                command.data.id,
                                dependencies.first { it.name == name }.locateField(obj.id.id.second, command.data.id)
                            )
                        }
                    }
                    check(data == fieldId.type) {
                        "Field type mismatch! ${
                            processDebug(
                                lastDebugNumber,
                                lastDebugLine
                            )
                        }Wanted: ${fieldId.type::class} Got: ${data::class}"
                    }
                }
                is SimpleValue.NewObject -> {
                    var cId = 0u
                    val objId = if (command.data is LocationRef.LocationByName) {
                        val id = resolveDepObject(self, dependencies, depMap, command.data.name.toString())
                        cId = id.cId
                        replaceList[index - indexOffset] =
                            SimpleValue.NewObject(LocationRef.LocationByCId(Pair(id.cId, id.id)))
                        id
                    } else {
                        command.data as LocationRef.LocationByCId
                        if (command.data.id.first == 0u) {
                            IdObject(
                                command.data.id.second,
                                command.data.id.first,
                                self.locateObject(command.data.id.second)
                            )
                        } else {
                            val name = depMap.first { it.id == command.data.id.first }.name
                            val obj = IdObject(
                                command.data.id.second,
                                command.data.id.first,
                                dependencies.first { it.name == name }.locateObject(command.data.id.second)
                            )
                            cId = obj.cId
                            obj
                        }
                    }

                    if (objId.obj is PublicObject) {
                        stack.push(
                            Type.ObjRef(
                                objId.obj.fields.map { it.type },
                                LocationRef.LocationByCId(Pair(cId, objId.id))
                            )
                        )
                    } else {
                        objId.obj as UnlinkedObject
                        stack.push(
                            Type.ObjRef(
                                objId.obj.fields.map { it.type },
                                LocationRef.LocationByCId(Pair(cId, objId.id))
                            )
                        )
                    }
                }
                is SimpleValue.Launch -> {
                    val methId = if (command.data is LocationRef.LocationByName) {
                        val id = resolveDepMethod(self, dependencies, depMap, command.data.name.toString())
                        replaceList[index - indexOffset] =
                            SimpleValue.Launch(LocationRef.LocationByCId(Pair(id.cId, id.id)))
                        id
                    } else {
                        command.data as LocationRef.LocationByCId
                        if (command.data.id.first == 0u) {
                            IdMethod(
                                command.data.id.second,
                                command.data.id.first,
                                self.locateMethod(command.data.id.second)
                            )
                        } else {
                            val name = depMap.first { it.id == command.data.id.first }.name
                            IdMethod(
                                command.data.id.second,
                                command.data.id.first,
                                dependencies.first { it.name == name }.locateMethod(command.data.id.second)
                            )
                        }
                    }
                    methId.method as UnlinkedMethod
                    methId.method.args.forEach {
                        val popped = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}INVOKE")
                        check(popped == it.type) {
                            "Type mismatch! ${
                                processDebug(
                                    lastDebugNumber,
                                    lastDebugLine
                                )
                            }Wanted: ${it.type::class} Got: ${popped::class}"
                        }
                    }
                    check(methId.method.returnType == Type.Unit) {
                        "Launch on method with return type! ${processDebug(lastDebugNumber, lastDebugLine)}"
                    }
                }
                is SimpleValue.Invoke -> {
                    val methId = if (command.data is LocationRef.LocationByName) {
                        val id = resolveDepMethod(self, dependencies, depMap, command.data.name.toString())
                        replaceList[index - indexOffset] =
                            SimpleValue.Invoke(LocationRef.LocationByCId(Pair(id.cId, id.id)))
                        id
                    } else {
                        command.data as LocationRef.LocationByCId
                        if (command.data.id.first == 0u) {
                            IdMethod(
                                command.data.id.second,
                                command.data.id.first,
                                self.locateMethod(command.data.id.second)
                            )
                        } else {
                            val name = depMap.first { it.id == command.data.id.first }.name
                            IdMethod(
                                command.data.id.second,
                                command.data.id.first,
                                dependencies.first { it.name == name }.locateMethod(command.data.id.second)
                            )
                        }
                    }
                    if (methId.method is UnlinkedMethod) {
                        methId.method.args.forEach {
                            val popped = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}INVOKE")
                            check(popped == it.type) {
                                "Type mismatch! ${
                                    processDebug(
                                        lastDebugNumber,
                                        lastDebugLine
                                    )
                                }Wanted: ${it.type::class} Got: ${popped::class}"
                            }
                        }
                        if (methId.method.returnType != Type.Unit) {
                            stack.push(methId.method.returnType)
                        }
                    } else {
                        methId.method as PublicMethod
                        methId.method.args.forEach {
                            val popped = safePop(stack, "${processDebug(lastDebugNumber, lastDebugLine)}INVOKE")
                            check(popped == it) {
                                "Type mismatch! ${
                                    processDebug(
                                        lastDebugNumber,
                                        lastDebugLine
                                    )
                                }Wanted: ${it::class} Got: ${popped::class}"
                            }
                        }
                        if (methId.method.returns != Type.Unit) {
                            stack.push(methId.method.returns)
                        }
                    }
                }
                else -> error("Unrecognized command: ${command::class} ${processDebug(lastDebugNumber, lastDebugLine)}")
            }
            index++
        }

        replaceList.forEach { (t, u) ->
            instructions[t] = u
        }

        return index
    }

    private fun resolveDepField(
        thisDep: UnlinkedContainer,
        rest: Set<APIContainer>,
        mapping: List<NameId>,
        obj: LocationRef.LocationByCId,
        target: String
    ): IdType {
        val container = if (thisDep.getSelfMapping().containsKey(target)) {
            thisDep
        } else {
            val resName = mapping.first { it.id == obj.id.first }
            rest.first { it.name == resName.name }
        }
        val id = container.locateField(obj.id.second, target)!!
        return IdType(id, container.locateField(obj.id.second, id))
    }

    private fun resolveDepMethod(
        thisDep: UnlinkedContainer,
        rest: Set<APIContainer>,
        mapping: List<NameId>,
        target: String
    ): IdMethod {
        val container = if (thisDep.getSelfMapping().containsKey(target)) {
            thisDep
        } else {
            val res = rest.firstOrNull { it.locateMethod(target) != null }
            check(res != null) {
                "Could not find method: [$target]"
            }
            res
        }
        val id = container.locateMethod(target)!!
        return IdMethod(id, mapping.first { it.name == container.name }.id, container.locateMethod(id))
    }

    private fun resolveDepObject(
        thisDep: UnlinkedContainer,
        rest: Set<APIContainer>,
        mapping: List<NameId>,
        target: String
    ): IdObject {
        val container = if (thisDep.getSelfMapping().containsKey(target)) {
            thisDep
        } else {
            val res = rest.firstOrNull { it.locateObject(target) != null }
            check(res != null) {
                "Could not find object: [$target]"
            }
            res
        }
        val id = container.locateObject(target)!!
        return IdObject(id, mapping.first { it.name == container.name }.id, container.locateObject(id))
    }

    private fun processDebug(debugLineNumber: UInt, debugLine: CharSequence): String {
        return if (debugLine.isNotBlank()) {
            "Error at Line: $debugLine:$debugLineNumber "
        } else if (debugLineNumber > 0u) {
            "Error at LineNumber: $debugLineNumber "
        } else {
            ""
        }
    }

    override fun size(): Int {
        return instructions.size
    }

    override fun toBulk(storage: MutableList<Command>) {
        storage.addAll(instructions)
    }

    override fun toBin(buffer: MemorySegmentBuffer) {
        buffer.write(1.toByte())
        buffer.writeVar(instructions.size)
        instructions.forEach {
            it.toBin(buffer)
        }
    }

    override fun sizeBytes(): Long {
        return 1L + MemorySegmentBuffer.varSize(instructions.size) + ceil(instructions.sumOf { it.sizeBits() } / 8.0).toLong()
    }
}
