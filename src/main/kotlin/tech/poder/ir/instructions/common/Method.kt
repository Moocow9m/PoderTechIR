package tech.poder.ir.instructions.common

import tech.poder.ir.instructions.simple.CodeBuilder

data class Method(
    val parent: Object = staticObject,
    val argCount: UByte = 0u,
    val name: String,
    val returns: Boolean,
    val code: ArrayList<Instruction>
) {
    companion object {
        val staticObject = Object("static", null, emptySet())
        fun create(
            name: String,
            argCount: UByte = 0u,
            returns: Boolean = false,
            parent: Object? = null,
            code: (CodeBuilder) -> Unit
        ): Method {
            val trueParent = parent ?: staticObject
            val builder = CodeBuilder(returns, trueParent.nameSpace)
            code.invoke(builder)
            return Method(trueParent, argCount, name, returns, builder.code())
        }
    }

    val fullName by lazy {
        "${parent.nameSpace}.$name"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Method) return false
        if (!fullName.equals(other.fullName, false)) return false
        return argCount == other.argCount
    }

    override fun hashCode(): Int {
        var result = argCount.hashCode()
        result = 31 * result + fullName.hashCode()
        return result
    }
}