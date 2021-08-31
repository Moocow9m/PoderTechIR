package tech.poder.ir.data.base

import tech.poder.ir.data.CodeBuilder
import tech.poder.ir.data.storage.ConstantPool
import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.data.storage.Type
import tech.poder.ir.metadata.Visibility

data class Package(
    val namespace: String,
    val visibility: Visibility,
    val objects: MutableSet<Object> = mutableSetOf(),
    val floating: MutableSet<Method> = mutableSetOf(),
    val constPool: ConstantPool = ConstantPool(mutableMapOf()),
    val requiredLibs: MutableSet<String> = mutableSetOf(),
) {
    fun newFloatingMethod(
        name: String,
        vis: Visibility,
        returnType: Type? = null,
        vararg args: NamedType,
        code: (CodeBuilder) -> Unit
    ): Method {
        val meth = CodeBuilder.createMethod(this, name, vis, returnType, args.toSet(), null, code)
        floating.add(meth)
        return meth
    }

    fun newObject(name: String, vis: Visibility, vararg fields: NamedType): Object {
        val obj = Object(
            this,
            name,
            vis,
            mutableSetOf(),
            fields.map { NamedType("${namespace}.$name.${it.name}", it.type) }.toTypedArray()
        )
        objects.add(obj)
        return obj
    }

    fun finalize() {
        //todo this will remove names from all methods and objects marked private and replace the references with internal id numbers
    }

    override fun hashCode(): Int {
        return namespace.hashCode()
    }


    override fun toString(): String {
        return "$visibility package $namespace {\n\t${floating.joinToString("\n\t")}\n\n${
            objects.joinToString("\n") {
                it.toString(
                    1
                )
            }
        }\n}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Package) return false

        if (namespace != other.namespace) return false

        return true
    }
}