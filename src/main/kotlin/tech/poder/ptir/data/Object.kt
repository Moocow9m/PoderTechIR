package tech.poder.ptir.data

data class Object internal constructor(
    val parent: Package,
    val name: String,
    val methods: MutableSet<Method>,
    val fields: MutableSet<Field>
) {
    fun newMethod(name: String, returnType: Type?, vararg args: Arg, code: (CodeBuilder) -> Unit) {
        methods.add(CodeBuilder.createMethod(parent, name, returnType, args.toSet(), this, code))
    }
}
