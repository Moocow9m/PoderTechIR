package tech.poder.ptir.data.base

import tech.poder.ptir.data.CodeBuilder
import tech.poder.ptir.data.storage.NamedType
import tech.poder.ptir.data.storage.Type

data class Object internal constructor(
    val parent: Package,
    val name: String,
    internal val methods: MutableSet<Method>,
    internal val fields: Array<NamedType>
) {

    val fullName by lazy {
        "${parent.namespace}.$name"
    }

    fun newMethod(name: String, returnType: Type?, vararg args: NamedType, code: (CodeBuilder) -> Unit): Method {
        val meth = CodeBuilder.createMethod(
            parent,
            name,
            returnType,
            setOf(NamedType("this", Type.TStruct(fields)), *args),
            this,
            code
        )
        methods.add(meth)
        return meth
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Object) return false

        if (parent != other.parent) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parent.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String {
        return "class $fullName {\n\t${fields.joinToString("\n\t")}\n\n\t${methods.joinToString("\n\t")}\n}"
    }
}
