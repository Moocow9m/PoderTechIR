package tech.poder.ptir.data

import java.util.*

data class CodeBuilder(
    private val storage: Method,
    val stack: Stack<Type> = Stack(),
    val instructions: ArrayList<Instruction> = arrayListOf()
) {
    companion object {
        fun createMethod(
            package_: Package,
            name: String,
            returnType: Type? = null,
            args: Set<Arg> = emptySet(),
            parent: Object? = null,
            block: (CodeBuilder) -> Unit
        ): Method {
            val method = Method(package_, parent, name, returnType, args, emptyArray())

            val builder = CodeBuilder(method)
            block.invoke(builder)

            method.instructions = builder.finalize()
            return method
        }
    }

    fun finalize(): Array<Instruction> {
        //todo Validation and minor code merging of constant ops

        return instructions.toTypedArray()
    }
}