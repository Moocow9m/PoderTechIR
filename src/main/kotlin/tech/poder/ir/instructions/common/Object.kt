package tech.poder.ir.instructions.common

import tech.poder.ir.vm.ObjectInstance

data class Object(val nameSpace: String, val struct: Struct?, val methods: List<Method>) {
    fun createInstance(): ObjectInstance {
        return ObjectInstance(nameSpace, Array(struct!!.types.size) { struct.types[it].default })
    }
}