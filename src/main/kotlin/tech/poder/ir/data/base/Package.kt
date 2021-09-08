package tech.poder.ir.data.base

import tech.poder.ir.api.CodeHolder
import tech.poder.ir.data.CodeBuilder
import tech.poder.ir.data.Type
import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.metadata.Visibility

data class Package internal constructor(
    val namespace: String,
    val visibility: Visibility = Visibility.PRIVATE,
    internal val objects: MutableSet<Object> = mutableSetOf(),
    internal val floating: MutableSet<Method> = mutableSetOf(),
) : CodeHolder {

    fun newFloatingMethod(
        name: String,
        vis: Visibility = Visibility.PRIVATE,
        returnType: Type = Type.Unit,
        args: Set<NamedType> = emptySet(),
        code: (CodeBuilder) -> Unit
    ): Method {
        val meth = Method(this, null, name, returnType, args, vis)
        val builder = CodeBuilder(meth)
        code.invoke(builder)
        builder.finalize()
        floating.add(meth)
        return meth
    }

    fun newObject(name: String, vis: Visibility = Visibility.PRIVATE, fields: Set<NamedType> = emptySet()): Object {
        return Object(
            this,
            name,
            vis,
            mutableSetOf(),
            fields.map { NamedType("${namespace}.$name.${it.name}", it.type) }
        ).apply {
            objects.add(this)
        }
    }

    fun finalize() {
        //todo this will remove names from all methods and objects marked private and replace the references with internal id numbers
    }

    override fun hashCode(): Int {
        return namespace.hashCode()
    }


    override fun toString(): String {
        return (
            """
            $visibility package $namespace {
                   ${floating.joinToString("\n\t")}
                   ${objects.joinToString("\n") { it.toString(1) }}
            }       
            """.trimIndent()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Package) return false

        if (namespace != other.namespace) return false

        return true
    }
}
