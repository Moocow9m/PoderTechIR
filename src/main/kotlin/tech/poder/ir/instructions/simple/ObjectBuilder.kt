package tech.poder.ir.instructions.simple

import tech.poder.ir.instructions.common.Method
import tech.poder.ir.instructions.common.Object
import tech.poder.ir.instructions.common.Struct
import tech.poder.ir.instructions.common.types.Type

class ObjectBuilder(private val nameSpace: String, val hasFields: Boolean = true, init: (CodeBuilder) -> Unit) {
    private val methods = mutableListOf<Method>()
    private var nextId = 0u
    private val fieldsId = mutableMapOf<String, UInt>()
    private val fields = mutableMapOf<UInt, Pair<String, Type>>()
    private val dummyObject = Object(nameSpace, null, emptyList())

    init {
        createMethod("init", code = init)
    }

    fun createMethod(
        name: String,
        argCount: UByte = 0u,
        returns: Boolean = false,
        code: (CodeBuilder) -> Unit
    ): ObjectBuilder {
        val realArgCount: UByte = if (hasFields) {
            (argCount + 1u).toUByte()
        } else {
            argCount
        }
        val method = Method.create(name, realArgCount, returns, dummyObject, code)
        if (hasFields) {
            method.code.forEach {
                if (it.opcode == Simple.GET_FIELD || it.opcode == Simple.SET_FIELD) {
                    val fieldName = it.extra[0] as String
                    if (fieldName.startsWith(nameSpace)) {
                        val type = it.extra[1] as Type
                        val id = fieldsId.getOrPut(fieldName) {
                            nextId++
                        }
                        val tmp = fields.getOrPut(id) {
                            Pair(fieldName, it.extra[1] as Type)
                        }
                        check(tmp.second == type) {
                            "Field type error: $tmp != $type for field: $fieldName"
                        }
                        it.extra = arrayOf(id)
                    }
                }
            }
        }
        methods.add(method)
        return this
    }

    fun build(): Object {
        val names = Array(fields.size) { "" }
        val types = Array(fields.size) { Type.values()[0] }
        fields.toSortedMap().forEach {
            names[it.key.toInt()] = it.value.first
            types[it.key.toInt()] = it.value.second
        }
        return Object(nameSpace, Struct.build(types, names), methods)
    }
}