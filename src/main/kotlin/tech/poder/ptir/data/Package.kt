package tech.poder.ptir.data

data class Package(
    val namespace: String,
    val objects: MutableSet<Object>,
    val floating: MutableSet<Method>,
    val constPool: ConstantPool,
    val requiredLibs: MutableSet<String>
) {
    fun newFloatingMethod(name: String, returnType: Type?, vararg args: Arg, code: (CodeBuilder) -> Unit) {
        floating.add(CodeBuilder.createMethod(this, name, returnType, args.toSet(), null, code))
    }

    fun newObject(name: String): Object {
        val obj = Object(this, name, mutableSetOf(), mutableSetOf())
        objects.add(obj)
        return obj
    }
}
