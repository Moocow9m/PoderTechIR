package tech.poder.ptir.data.base

import tech.poder.ptir.data.CodeBuilder
import tech.poder.ptir.data.storage.ConstantPool
import tech.poder.ptir.data.storage.NamedType
import tech.poder.ptir.data.storage.Type

data class Package(
    val namespace: String,
    val objects: MutableSet<Object>,
    val floating: MutableSet<Method>,
    val constPool: ConstantPool,
    val requiredLibs: MutableSet<String>
) {
    fun newFloatingMethod(name: String, returnType: Type?, vararg args: NamedType, code: (CodeBuilder) -> Unit) {
        floating.add(CodeBuilder.createMethod(this, name, returnType, args.toSet(), null, code))
    }

    fun newObject(name: String, vararg fields: NamedType): Object {
        val obj = Object(this, name, mutableSetOf(), fields.toSet().toTypedArray())
        objects.add(obj)
        return obj
    }
}
