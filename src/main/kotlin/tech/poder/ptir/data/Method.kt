package tech.poder.ptir.data

data class Method internal constructor(
    val package_: Package,
    val parent: Object?,
    val name: String,
    val returnType: Type?,
    val args: Set<Arg>,
    var instructions: Array<Instruction>
) {
    val fullName by lazy {
        val builder = StringBuilder(package_.namespace)
        if (parent != null) {
            builder.append(parent.name)
        }
        builder.append(name)
        builder.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Method) return false

        if (package_ != other.package_) return false
        if (parent != other.parent) return false
        if (name != other.name) return false
        if (returnType != other.returnType) return false
        if (args != other.args) return false

        return true
    }

    override fun hashCode(): Int {
        var result = package_.hashCode()
        result = 31 * result + (parent?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + returnType.hashCode()
        result = 31 * result + args.hashCode()
        return result
    }
}
