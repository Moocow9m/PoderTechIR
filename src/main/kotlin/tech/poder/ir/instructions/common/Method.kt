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
            val builder = CodeBuilder(returns)
            code.invoke(builder)
            return if (parent == null) {
                Method(argCount = argCount, name = name, returns = returns, code = builder.code())
            } else {
                Method(parent, argCount, name, returns, builder.code())
            }
        }
    }
}