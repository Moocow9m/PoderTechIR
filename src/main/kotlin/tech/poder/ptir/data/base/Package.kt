package tech.poder.ptir.data.base

import tech.poder.ptir.data.CodeBuilder
import tech.poder.ptir.data.storage.ConstantPool
import tech.poder.ptir.data.storage.NamedType
import tech.poder.ptir.data.storage.Type

data class Package(
        val namespace: String,
        val objects: MutableSet<Object> = mutableSetOf(),
        val floating: MutableSet<Method> = mutableSetOf(),
        val constPool: ConstantPool = ConstantPool(mutableMapOf()),
        val requiredLibs: MutableSet<String> = mutableSetOf()
) {
    fun newFloatingMethod(
            name: String,
            returnType: Type? = null,
            vararg args: NamedType,
            code: (CodeBuilder) -> Unit
    ): Method {
        val meth = CodeBuilder.createMethod(this, name, returnType, args.toSet(), null, code)
        floating.add(meth)
        return meth
    }

    fun newObject(name: String, vararg fields: NamedType): Object {
        val obj = Object(
                this,
                name,
                mutableSetOf(),
                fields.map { NamedType("${namespace}.$name.${it.name}", it.type) }.toTypedArray()
        )
        objects.add(obj)
        return obj
    }

    override fun hashCode(): Int {
        return namespace.hashCode()
    }


    override fun toString(): String {
        return "package $namespace {\n\t${floating.joinToString("\n\t")}\n\n${objects.joinToString("\n") { it.toString(1) }}\n}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Package) return false

        if (namespace != other.namespace) return false

        return true
    }
}
