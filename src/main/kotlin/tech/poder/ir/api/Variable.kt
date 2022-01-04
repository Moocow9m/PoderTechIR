package tech.poder.ir.api

import tech.poder.ptir.PTIR

@JvmInline
value class Variable private constructor(internal val id: List<Any>) {
	companion object {
		var last = 0u
		val ARGS = Variable(listOf(true, 0u))

		fun newLocal(builder: MethodBuilder): Variable {
			return Variable(listOf(true, builder.varId++))
		}

		fun newGlobal(): Variable {
			return Variable(listOf(false, last++))
		}
	}


	fun toPTIR(): PTIR.Variable {
		return PTIR.Variable(id[0] as Boolean, id[1] as UInt)
	}
}
