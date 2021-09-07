package tech.poder.ptir.data.base

import tech.poder.ir.data.CodeBuilder
import tech.poder.ir.data.storage.ConstantPool
import tech.poder.ptir.api.CodeHolder
import tech.poder.ptir.data.Type
import tech.poder.ptir.data.storage.NamedType
import tech.poder.ptir.metadata.Visibility

data class Package internal constructor(
    val namespace: String,
    val visibility: Visibility,
    internal var children: MutableSet<Package> = mutableSetOf(),
    internal val objects: MutableSet<Object> = mutableSetOf(),
    internal val floating: MutableSet<Method> = mutableSetOf(),
    internal val constPool: ConstantPool = ConstantPool(mutableMapOf()),
) : CodeHolder {

    fun newChildPackage(name: String, vis: Visibility): Package {
        val pkg = Package(name, vis)
        children.add(pkg)
        return pkg
    }

    fun newFloatingMethod(
        name: String,
        vis: Visibility,
        returnType: Type = Type.Unit,
        args: Set<NamedType> = emptySet(),
        code: (CodeBuilder) -> Unit
    ): Method {
        return CodeBuilder.createMethod(this, name, vis, returnType, args, null, code).apply {
            floating.add(this)
        }
    }

    fun newObject(name: String, vis: Visibility, fields: Set<NamedType> = emptySet()): Object {
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
