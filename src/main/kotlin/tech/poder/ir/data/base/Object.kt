package tech.poder.ir.data.base

import tech.poder.ir.data.CodeBuilder
import tech.poder.ir.data.Type
import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.metadata.Visibility

data class Object internal constructor(
    val parent: Package,
    val name: String,
    val visibility: Visibility,
    internal val methods: MutableSet<Method>,
    internal val fields: List<NamedType>
) {

    val fullName by lazy {
        "${parent.namespace}_$name"
    }

    fun newMethod(
        name: String,
        vis: Visibility = Visibility.PRIVATE,
        returnType: Type = Type.Unit,
        args: Set<NamedType> = emptySet(),
        code: (CodeBuilder) -> Unit
    ): Method {
        val meth = Method(
            parent,
            this,
            name,
            returnType,
            setOf(NamedType("this", Type.Struct(fullName, fields)), *args.toTypedArray()),
            vis
        )
        val builder = CodeBuilder(meth)
        code.invoke(builder)
        builder.finalize()
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
        return (
            """
            $visibility class $fullName {        
                ${fields.joinToString("\n\t")}
                
                ${methods.joinToString("\n\t")}
            }
            """.trimIndent()
        )
    }

    fun toString(tabCount: Int): String {
        return toString().prependIndent("\t".repeat(tabCount))
        /*
        val tabs = "\t".repeat(tabCount)

        return (
            """
            $tabs$visibility class $fullName {
            $tabs${fields.joinToString("\n") { it.toString(tabCount + 1) }}
    
            
            $tabs${methods.joinToString("\n") { it.toString(tabCount + 1) }}
                
            $tabs}
            """.trimIndent()
        )
        */
    }
}
