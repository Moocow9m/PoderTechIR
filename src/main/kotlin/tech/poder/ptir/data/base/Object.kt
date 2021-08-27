package tech.poder.ptir.data.base

import tech.poder.ptir.data.CodeBuilder
import tech.poder.ptir.data.storage.NamedType
import tech.poder.ptir.data.storage.Type

data class Object internal constructor(
    val parent: Package,
    val name: String,
    val methods: MutableSet<Method>,
    val fields: MutableSet<Field>
) {
    fun newMethod(name: String, returnType: Type?, vararg args: NamedType, code: (CodeBuilder) -> Unit) {
        methods.add(CodeBuilder.createMethod(parent, name, returnType, args.toSet(), this, code))
    }
}
