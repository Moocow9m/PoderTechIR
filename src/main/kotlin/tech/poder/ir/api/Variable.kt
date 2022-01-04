package tech.poder.ir.api

@JvmInline
value class Variable private constructor(internal val id: List<UInt>) {
	companion object {
		var last = 0u
		val ARGS = Variable(listOf(0u, 0u))

		fun newLocal(builder: MethodBuilder): Variable {
			return Variable(listOf(0u, builder.varId++))
		}

		fun newGlobal(): Variable {
			return Variable(listOf(1u, last++))
		}
	}
}
