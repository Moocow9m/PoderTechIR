package tech.poder.ir.instructions.common

import tech.poder.ir.instructions.simple.CodeBuilder

data class Method(
    val parent: Object = Object("static", null, emptyList()),
    val argCount: UByte = 0u,
    val name: String,
    val returns: Boolean,
    val code: ArrayList<Instruction>
) {
    companion object {
        fun create(
            name: String,
            argCount: UByte = 0u,
            returns: Boolean = false,
            parent: Object? = null,
            code: (CodeBuilder) -> Unit
        ): Method {
            val trueParent = parent ?: Object("static", null, emptyList())
            val builder = CodeBuilder(returns, trueParent.nameSpace, name)
            code.invoke(builder)
            return Method(trueParent, argCount, name, returns, builder.code())
        }
    }
}